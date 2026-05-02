package ru.practicum.explorewithme.service.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.category.dal.CategoryRepository;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.dto.*;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.enums.UserEventStateAction;
import ru.practicum.explorewithme.service.event.mapper.EventMapper;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.event.service.predicate.EventPredicate;
import ru.practicum.explorewithme.service.exception.BadRequestException;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.request.dal.EventRequestRepository;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.user.dal.UserRepository;
import ru.practicum.explorewithme.stats.client.StatsClient;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события пользователем id={}", userId);
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), EventMapper.FORMATTER);
        if (ChronoUnit.HOURS.between(LocalDateTime.now(), eventDate) < 2) {
            throw new BadRequestException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }

        Event event = EventMapper.toEntity(newEventDto);
        event.setInitiator(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден")));
        event.setCategory(categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена")));
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);
        log.debug("Событие сохранено с id={}", event.getId());
        return EventMapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        log.info("Получение событий пользователя id={}, from={}, size={}", userId, from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(EventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        log.info("Получение события id={} пользователя id={}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено или недоступно"));
        return EventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Обновление события id={} пользователем id={}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено или недоступно"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя редактировать опубликованное событие");
        }

        if (request.getStateAction() != null) {
            if (request.getStateAction() == UserEventStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            } else if (request.getStateAction() == UserEventStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            }
        }

        if (request.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(request.getEventDate(), EventMapper.FORMATTER);
            if (ChronoUnit.HOURS.between(LocalDateTime.now(), newDate) < 2) {
                throw new BadRequestException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
            }
        }

        EventMapper.updateEntityFromRequest(request, event);
        eventRepository.save(event);
        log.debug("Событие обновлено");
        return EventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto getPublishedEvent(Long eventId) {
        log.info("Получение опубликованного события id={}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено или недоступно");
        }

        Long views = 0L;
        try {
            List<ViewStatsDTO> stats = statsClient.getStats(
                    LocalDateTime.of(2000, 1, 1, 0, 0),
                    LocalDateTime.now().plusDays(1),
                    List.of("/events/" + eventId),
                    false);
            if (stats != null && !stats.isEmpty()) {
                views = stats.get(0).getHits();
            }
        } catch (Exception e) {
            log.warn("Не удалось получить статистику для события {}: {}", eventId, e.getMessage());
        }

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(
                eventId, ParticipationRequestStatus.CONFIRMED);

        return EventMapper.toFullDto(event, views, confirmedRequests);
    }

    @Override
    public List<EventShortDto> getEvents(EventSearchParams params) {
        BooleanExpression predicate = EventPredicate.build(params);

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), getSort(params.getSort()));

        Page<Event> page = eventRepository.findAll(predicate, pageable);

        List<Event> events = page.getContent();

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        Map<Long, Long> viewsMap = new HashMap<>();
        try {
            List<ViewStatsDTO> stats = statsClient.getStats(
                    LocalDateTime.of(2000, 1, 1, 0, 0),
                    LocalDateTime.now().plusDays(1),
                    uris,
                    false);
            if (stats != null) {
                for (ViewStatsDTO stat : stats) {
                    String uri = stat.getUri();
                    Long eventId = Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
                    viewsMap.put(eventId, stat.getHits());
                }
            }
        } catch (Exception e) {
            log.warn("Не удалось получить статистику для событий: {}", e.getMessage());
        }

        List<EventShortDto> list = events.stream()
                .map(event -> {
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    Long confirmedRequests = requestRepository.countByEventIdAndStatus(
                            event.getId(), ParticipationRequestStatus.CONFIRMED);
                    return EventMapper.toShortDto(event, views, confirmedRequests);
                })
                .toList();

        log.info("Список событий после фильтрации {}", list);
        return list;
    }

    private Sort getSort(String sort) {
        if ("VIEWS".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "views");
        }
        return Sort.by(Sort.Direction.ASC, "eventDate");
    }
}
