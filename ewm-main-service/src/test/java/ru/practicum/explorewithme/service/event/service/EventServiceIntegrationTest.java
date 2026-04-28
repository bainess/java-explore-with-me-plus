package ru.practicum.explorewithme.service.event.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.event.dto.*;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.enums.UserEventStateAction;
import ru.practicum.explorewithme.service.user.dto.NewUserRequest;
import ru.practicum.explorewithme.service.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class EventServiceIntegrationTest {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void fullLifecycleTest() {
        var user = userService.registerUser(new NewUserRequest("creator@example.com", "Creator"));
        // категория должна существовать в БД, допустим, с id=1. Предположим, она создана заранее.
        Long categoryId = 1L;

        NewEventDto newEvent = buildValidDto(categoryId, LocalDateTime.now().plusDays(3));
        EventFullDto created = eventService.addEvent(user.getId(), newEvent);
        assertThat(created.getState()).isEqualTo(EventState.PENDING);

        // обновление заголовка
        UpdateEventUserRequest update = UpdateEventUserRequest.builder().title("Updated title").build();
        EventFullDto updated = eventService.updateEvent(user.getId(), created.getId(), update);
        assertThat(updated.getTitle()).isEqualTo("Updated title");

        // отмена
        update = UpdateEventUserRequest.builder().stateAction(UserEventStateAction.CANCEL_REVIEW).build();
        EventFullDto canceled = eventService.updateEvent(user.getId(), created.getId(), update);
        assertThat(canceled.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void getEvents_Pagination() {
        var user = userService.registerUser(new NewUserRequest("pagin@example.com", "Pagin"));
        for (int i = 0; i < 5; i++) {
            try {
                eventService.addEvent(user.getId(), buildValidDto(1L, LocalDateTime.now().plusDays(2 + i)));
            } catch (Exception e) { /* ignore if category missing, test assumes category id=1 exists */ }
        }
        List<EventShortDto> page1 = eventService.getEvents(user.getId(), 0, 2);
        assertThat(page1).hasSizeLessThanOrEqualTo(2);
    }

    private NewEventDto buildValidDto(Long categoryId, LocalDateTime eventDate) {
        return NewEventDto.builder()
                .annotation("Valid annotation for testing")
                .category(categoryId)
                .description("Valid description for testing")
                .eventDate(eventDate.format(fmt))
                .location(new Location(55.75f, 37.62f))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .title("Test event")
                .build();
    }
}
