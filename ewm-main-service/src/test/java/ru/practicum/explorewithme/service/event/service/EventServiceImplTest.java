package ru.practicum.explorewithme.service.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.explorewithme.service.category.dal.CategoryRepository;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.dto.*;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.enums.UserEventStateAction;
import ru.practicum.explorewithme.service.event.mapper.EventMapper;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.user.dal.UserRepository;
import ru.practicum.explorewithme.service.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @InjectMocks
    private EventServiceImpl eventService;

    private User user;
    private Category category;
    private NewEventDto newEventDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "user@example.com", "User");
        category = new Category(1L, "Концерты");
        newEventDto = NewEventDto.builder()
                .annotation("Valid annotation for testing")
                .category(1L)
                .description("Valid description for testing")
                .eventDate("2030-12-31 15:10:05")
                .location(new Location(55.75f, 37.62f))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .title("Valid title")
                .build();
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
                .isInstanceOf(ConflictException.class)
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
                .thenReturn(new PageImpl<>(List.of(new Event())));
        List<EventShortDto> result = eventService.getEvents(1L, 0, 10);
        assertThat(result).hasSize(1);
    }

    @Test
    void getEvent_ShouldReturnFullDto() {
        Event event = createEventWithDefaults();
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        EventFullDto result = eventService.getEvent(1L, 1L);
        assertThat(result.getId()).isEqualTo(event.getId());
    }

    @Test
    void getEvent_NotFound_ShouldThrowNotFound() {
        when(eventRepository.findByIdAndInitiatorId(1L, 999L)).thenReturn(Optional.empty());
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
}
