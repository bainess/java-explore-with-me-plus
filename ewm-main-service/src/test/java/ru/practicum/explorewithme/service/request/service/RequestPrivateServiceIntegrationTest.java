package ru.practicum.explorewithme.service.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.service.CategoryService;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.LocationDto;
import ru.practicum.explorewithme.service.event.dto.NewEventDto;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class RequestPrivateServiceIntegrationTest {

    @Autowired
    private EventRequestService requestService;
    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private EventRepository eventRepository;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Long categoryId;
    private Long initiatorId;
    private Long requesterId;

    @BeforeEach
    void setUp() {
        CategoryDto cat = categoryService.createCategory(new NewCategoryRequest("Test Category"));
        categoryId = cat.getId();

        var initiator = userService.registerUser(new NewUserRequest("init@example.com", "Initiator"));
        initiatorId = initiator.getId();

        var requester = userService.registerUser(new NewUserRequest("req@example.com", "Requester"));
        requesterId = requester.getId();
    }

    private EventFullDto createAndPublishEvent(Long initiatorId, NewEventDto dto) {
        EventFullDto event = eventService.addEvent(initiatorId, dto);
        Event eventEntity = eventRepository.findById(event.getId()).orElseThrow();
        eventEntity.setState(EventState.PUBLISHED);
        eventRepository.saveAndFlush(eventEntity);
        return event;
    }

    @Test
    void shouldAddAndCancelRequest() {
        NewEventDto dto = NewEventDto.builder()
                .annotation("Test annotation for integration")
                .category(categoryId)
                .description("Test description for integration test")
                .eventDate(LocalDateTime.now().plusDays(2).format(fmt))
                .location(new LocationDto(55.75f, 37.62f))
                .requestModeration(true)
                .participantLimit(10)
                .title("Integration Test Event")
                .build();
        EventFullDto event = createAndPublishEvent(initiatorId, dto);

        ParticipationRequestDto request = requestService.addParticipationRequest(requesterId, event.getId());
        assertThat(request.getStatus()).isEqualTo(ParticipationRequestStatus.PENDING);

        List<ParticipationRequestDto> requests = requestService.getUserRequests(requesterId);
        assertThat(requests).hasSize(1);

        ParticipationRequestDto cancelled = requestService.cancelRequest(requesterId, request.getId());
        assertThat(cancelled.getStatus()).isEqualTo(ParticipationRequestStatus.CANCELED);
    }

    @Test
    void shouldAutoConfirmWhenModerationDisabled() {
        NewEventDto dto = NewEventDto.builder()
                .annotation("Auto confirm event")
                .category(categoryId)
                .description("Event with moderation disabled")
                .eventDate(LocalDateTime.now().plusDays(2).format(fmt))
                .location(new LocationDto(55.75f, 37.62f))
                .requestModeration(false)
                .participantLimit(10)
                .title("Auto Confirm Event")
                .build();
        EventFullDto event = createAndPublishEvent(initiatorId, dto);

        ParticipationRequestDto request = requestService.addParticipationRequest(requesterId, event.getId());
        assertThat(request.getStatus()).isEqualTo(ParticipationRequestStatus.CONFIRMED);
    }

    @Test
    void shouldRespectParticipantLimit() {
        NewEventDto dto = NewEventDto.builder()
                .annotation("Limit test event")
                .category(categoryId)
                .description("Event with participant limit 1")
                .eventDate(LocalDateTime.now().plusDays(2).format(fmt))
                .location(new LocationDto(55.75f, 37.62f))
                .requestModeration(false)
                .participantLimit(1)
                .title("Limit Test Event")
                .build();
        EventFullDto event = createAndPublishEvent(initiatorId, dto);

        requestService.addParticipationRequest(requesterId, event.getId());

        var requester2 = userService.registerUser(new NewUserRequest("req2@example.com", "Requester2"));
        try {
            requestService.addParticipationRequest(requester2.getId(), event.getId());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }
}
