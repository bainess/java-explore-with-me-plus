package ru.practicum.explorewithme.service.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.explorewithme.service.category.dal.CategoryRepository;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.dto.*;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.enums.UserEventStateAction;
import ru.practicum.explorewithme.service.event.mapper.EventMapper;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.event.model.Location;
import ru.practicum.explorewithme.service.exception.BadRequestException;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.user.dal.UserRepository;
import ru.practicum.explorewithme.service.user.model.User;
import ru.practicum.explorewithme.service.request.dal.EventRequestRepository;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventRequestRepository requestRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private User user;
    private Category category;
    private NewEventDto newEventDto;
    private Event event;

    @BeforeEach
    void setUp() {
        user = new User(1L, "user@example.com", "User");
        category = new Category(1L, "Концерты");
        newEventDto = NewEventDto.builder()
                .annotation("Valid annotation for testing")
                .category(1L)
                .description("Valid description for testing")
                .eventDate("2030-12-31 15:10:05")
                .location(new LocationDto(55.75f, 37.62f))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .title("Valid title")
                .build();
        event = new Event();
        event.setCategory(category);               // <-- установить тестовую категорию
        event.setInitiator(user);                  // <-- и инициатора (для маппера)
        event.setAnnotation("Valid annotation for testing");
        event.setDescription("Valid description for testing");
        event.setEventDate(LocalDateTime.parse("2030-12-31T15:10:05"));
        event.setLocation(new Location(55.75f, 37.62f));
        event.setPaid(false);
        event.setParticipantLimit(0);
        event.setRequestModeration(true);
        event.setTitle("Valid title");
    }

    @Test
    void addEvent_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Event savedEvent = EventMapper.toEntity(newEventDto);
        savedEvent.setId(1L);
        savedEvent.setInitiator(user);
        savedEvent.setCategory(category);
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        EventFullDto result = eventService.addEvent(1L, newEventDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getState()).isEqualTo(EventState.PENDING);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void addEvent_DateTooEarly_ShouldThrowConflict() {
        newEventDto.setEventDate(LocalDateTime.now().plusHours(1).format(EventMapper.FORMATTER));

        assertThatThrownBy(() -> eventService.addEvent(1L, newEventDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Дата события должна быть не ранее чем через 2 часа");
    }

    @Test
    void addEvent_UserNotFound_ShouldThrowNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.addEvent(99L, newEventDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getEvents_ShouldApplyPagination() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(eventRepository.findAllByInitiatorId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(requestRepository.countConfirmedRequestsByEventIds(anyList())).thenReturn(Collections.emptyList());

        List<EventShortDto> result = eventService.getEvents(1L, 0, 10);
        assertThat(result).hasSize(1);
    }

    @Test
    void getEvent_ShouldReturnFullDto() {
        Event event = createEventWithDefaults();
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(anyLong(), any(ParticipationRequestStatus.class))).thenReturn(0);

        EventFullDto result = eventService.getEvent(1L, 1L);
        assertThat(result.getId()).isEqualTo(event.getId());
    }


    @Test
    void getEvent_NotFound_ShouldThrowNotFound() {
        when(eventRepository.findByIdAndInitiatorId(999L, 1L)).thenReturn(Optional.empty());  // eventId=999, userId=1
        assertThatThrownBy(() -> eventService.getEvent(1L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateEvent_ChangeStateToCancel() {
        Event event = createEventWithDefaults();
        event.setState(EventState.PENDING);
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(UserEventStateAction.CANCEL_REVIEW)
                .build();
        when(eventRepository.save(any())).thenReturn(event);
        when(requestRepository.countByEventIdAndStatus(anyLong(), any(ParticipationRequestStatus.class))).thenReturn(0);

        EventFullDto result = eventService.updateEvent(1L, 1L, request);
        assertThat(result.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void updateEvent_Published_ShouldThrowConflict() {
        Event event = createEventWithDefaults();
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        UpdateEventUserRequest request = UpdateEventUserRequest.builder().title("New").build();
        assertThatThrownBy(() -> eventService.updateEvent(1L, 1L, request))
                .isInstanceOf(ConflictException.class);
    }

    private Event createEventWithDefaults() {
        Event event = new Event();
        event.setId(1L);
        event.setCategory(category);
        event.setInitiator(user);
        event.setAnnotation("annotation");
        event.setDescription("description");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setLocation(new ru.practicum.explorewithme.service.event.model.Location(55f, 37f));
        event.setPaid(false);
        event.setParticipantLimit(0);
        event.setRequestModeration(true);
        event.setState(EventState.PENDING);
        event.setTitle("title");
        event.setCreatedOn(LocalDateTime.now());
        return event;
    }

    @Test
    void shouldReturnEvents() {

        EventSearchParams params = EventSearchParams.builder()
                .from(0)
                .size(10)
                .sort("VIEWS")
                .build();

        Page<Event> page = new PageImpl<>(List.of(new Event()));

        when(eventRepository.findAll(ArgumentMatchers.<BooleanExpression>any(), any(Pageable.class)))
                .thenReturn(page);

        List<EventShortDto> result = eventService.getEventsPublic(params);

        assertFalse(result.isEmpty());
        verify(eventRepository, times(1))
                .findAll(any(BooleanExpression.class), any(Pageable.class));
    }

    @Test
    void shouldReturnEventById() {

        Event event = new Event();
        event.setId(1L);
        event.setState(EventState.PUBLISHED);

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        EventFullDto dto = eventService.getEventPublic(1L);

        assertNotNull(dto);
        verify(eventRepository).findById(1L);
    }
}
