package ru.practicum.explorewithme.service.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.EventShortDto;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.stats.client.StatsClient;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicEventControllerTest.class)
public class PublicEventControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventService eventService;

    @MockBean
    private StatsClient statsClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnEventsWithParams() throws Exception {

        List<EventShortDto> events = List.of(new EventShortDto());

        when(eventService.getEvents(any()))
                .thenReturn(events);

        mockMvc.perform(get("/events")
                        .param("text", "concert")
                        .param("categories", "1", "2")
                        .param("paid", "true")
                        .param("rangeStart", "2025-01-01 10:00:00")
                        .param("rangeEnd", "2025-01-02 10:00:00")
                        .param("onlyAvailable", "true")
                        .param("sort", "VIEWS")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getEvents(any());
    }

    @Test
    void shouldReturn400WhenEndBeforeStart() throws Exception {

        mockMvc.perform(get("/events")
                        .param("rangeStart", "2025-01-02 10:00:00")
                        .param("rangeEnd", "2025-01-01 10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnEventWithViews() throws Exception {

        Long eventId = 1L;

        EventFullDto event = new EventFullDto();
        event.setId(eventId);
        event.setCreatedOn("2025-01-01 10:00:00");

        when(eventService.getEvent(eventId)).thenReturn(event);

        List<ViewStatsDTO> statsList = List.of(
                new ViewStatsDTO("ewm-main-service", "/events/1", 5L)
        );

        ResponseEntity<List<ViewStatsDTO>> response =
                ResponseEntity.ok(statsList);

        when(statsClient.getStats(any(), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isOk());

        verify(statsClient, times(1)).saveHit(any());
        verify(statsClient, times(1)).getStats(any(), any(), any(), any());
    }
}
