package ru.practicum.explorewithme.service.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.request.dal.EventRequestRepository;
import ru.practicum.explorewithme.service.user.dal.UserRepository;
import ru.practicum.explorewithme.service.user.model.User;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.service.request.model.ParticipationRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRequestServiceImpl implements EventRequestService {

    private final EventRepository eventRepository;
    private final EventRequestRepository eventRequestRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение заявок на событие id={} пользователя id={}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено или недоступно"));
        List<ParticipationRequest> requests = eventRequestRepository
                .findAllByEventIdAndEventInitiatorId(eventId, userId);
        return requests.stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        log.info("Изменение статуса заявок на событие id={} пользователем id={}", eventId, userId);

        Event event = getEventAndValidateOwnership(userId, eventId);
        validateRequestPrerequisites(event);

        ParticipationRequestStatus newStatus = validateNewStatus(request.getStatus());
        List<ParticipationRequest> pendingRequests = getPendingRequestsOrThrow(request.getRequestIds());

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        if (newStatus == ParticipationRequestStatus.CONFIRMED) {
            processConfirmation(event, pendingRequests, confirmed, rejected);
        } else {
            // Проверяем, что не пытаемся отклонить уже подтвержденные заявки
            for (ParticipationRequest r : pendingRequests) {
                if (r.getStatus() == ParticipationRequestStatus.CONFIRMED) {
                    throw new ConflictException("Нелзя отменить уже подтвержденную заявку");
                }
            }
            rejectAll(pendingRequests, rejected);
        }

        eventRequestRepository.saveAll(pendingRequests);

        return buildResult(confirmed, rejected);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение заявок пользователя id={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        List<ParticipationRequest> requests = eventRequestRepository.findAllByRequesterId(userId);
        return requests.stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.info("Добавление заявки на событие id={} пользователем id={}", eventId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (event.getState() != ru.practicum.explorewithme.service.event.enums.EventState.PUBLISHED) {
            throw new ConflictException("Нельзя подать заявку на неопубликованное событие");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на свое событие");
        }

        if (eventRequestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Заявка на это событие уже подана");
        }

        int confirmedCount = eventRequestRepository.countByEventIdAndStatus(
                eventId, ParticipationRequestStatus.CONFIRMED);

        ParticipationRequestStatus status;
        if (event.getParticipantLimit() == 0) {
            status = ParticipationRequestStatus.CONFIRMED;
        } else if (!event.getRequestModeration()) {
            status = ParticipationRequestStatus.CONFIRMED;
        } else {
            status = ParticipationRequestStatus.PENDING;
        }

        if (status == ParticipationRequestStatus.CONFIRMED 
                && event.getParticipantLimit() > 0
                && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников исчерпан");
        }

        if (status == ParticipationRequestStatus.PENDING 
                && event.getParticipantLimit() > 0
                && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников исчерпан");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(status)
                .build();

        request = eventRequestRepository.save(request);
        return ParticipationRequestMapper.toDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена заявки id={} пользователем id={}", requestId, userId);

        ParticipationRequest request = eventRequestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Заявка с id=" + requestId + " не найдена"));

        if (request.getStatus() != ParticipationRequestStatus.PENDING) {
            throw new ConflictException("Можно отменить только заявку в статусе PENDING");
        }

        request.setStatus(ParticipationRequestStatus.CANCELED);
        request = eventRequestRepository.save(request);
        return ParticipationRequestMapper.toDto(request);
    }

    private Event getEventAndValidateOwnership(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено или недоступно"));
    }

    private void validateRequestPrerequisites(Event event) {
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Подтверждение заявок не требуется для данного события");
        }
    }

    private ParticipationRequestStatus validateNewStatus(ParticipationRequestStatus status) {
        if (status != ParticipationRequestStatus.CONFIRMED && status != ParticipationRequestStatus.REJECTED) {
            throw new ConflictException("Неверный статус заявки");
        }
        return status;
    }

    private List<ParticipationRequest> getPendingRequestsOrThrow(List<Long> requestIds) {
        List<ParticipationRequest> requests = eventRequestRepository.findAllByIdInAndStatus(
                requestIds, ParticipationRequestStatus.PENDING);
        if (requests.size() != requestIds.size()) {
            throw new ConflictException("Не все заявки находятся в состоянии ожидания");
        }
        return requests;
    }

    private void processConfirmation(Event event, List<ParticipationRequest> requests,
                                     List<ParticipationRequest> confirmed, List<ParticipationRequest> rejected) {
        int currentConfirmed = eventRequestRepository.countByEventIdAndStatus(
                event.getId(), ParticipationRequestStatus.CONFIRMED);
        int limit = event.getParticipantLimit();

        // Если лимит уже достигнут, нельзя подтверждать новые заявки
        if (currentConfirmed >= limit) {
            throw new ConflictException("Лимит участников уже достигнут, подтверждение заявок невозможно");
        }

        int remaining = limit - currentConfirmed;

        for (ParticipationRequest r : requests) {
            if (remaining > 0) {
                r.setStatus(ParticipationRequestStatus.CONFIRMED);
                confirmed.add(r);
                remaining--;
            } else {
                r.setStatus(ParticipationRequestStatus.REJECTED);
                rejected.add(r);
            }
        }

        // Автоматически отклонить все оставшиеся PENDING заявки, если лимит исчерпан
        if (remaining == 0) {
            rejectRemainingPending(event, rejected);
        }
    }

    private void rejectRemainingPending(Event event, List<ParticipationRequest> rejectedContainer) {
        List<ParticipationRequest> allPending = eventRequestRepository.findAllByEventIdAndStatus(
                event.getId(), ParticipationRequestStatus.PENDING);
        for (ParticipationRequest r : allPending) {
            r.setStatus(ParticipationRequestStatus.REJECTED);
            rejectedContainer.add(r);
        }
    }

    private void rejectAll(List<ParticipationRequest> requests, List<ParticipationRequest> rejected) {
        for (ParticipationRequest r : requests) {
            r.setStatus(ParticipationRequestStatus.REJECTED);
            rejected.add(r);
        }
    }

    private EventRequestStatusUpdateResult buildResult(List<ParticipationRequest> confirmed,
                                                       List<ParticipationRequest> rejected) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream().map(ParticipationRequestMapper::toDto).collect(Collectors.toList()))
                .rejectedRequests(rejected.stream().map(ParticipationRequestMapper::toDto).collect(Collectors.toList()))
                .build();
    }
}
