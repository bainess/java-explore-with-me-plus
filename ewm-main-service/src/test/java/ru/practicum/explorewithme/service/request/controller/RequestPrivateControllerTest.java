package ru.practicum.explorewithme.service.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.ErrorHandler;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.service.EventRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestPrivateController.class)
@Import(ErrorHandler.class)
class RequestPrivateControllerTest {

    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unused")
    @MockBean
    private EventRequestService eventRequestService;

    private ParticipationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = ParticipationRequestDto.builder()
                .id(100L)
                .requester(1L)
                .event(10L)
                .status(ParticipationRequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserRequests_Success() throws Exception {
        when(eventRequestService.getUserRequests(1L)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/{userId}/requests", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void addRequest_Success() throws Exception {
        when(eventRequestService.addParticipationRequest(eq(1L), eq(10L))).thenReturn(requestDto);

        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void addRequest_Conflict_ShouldReturn409() throws Exception {
        when(eventRequestService.addParticipationRequest(eq(1L), eq(10L)))
                .thenThrow(new ConflictException("Лимит участников исчерпан"));

        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "10"))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelRequest_Success() throws Exception {
        ParticipationRequestDto cancelledDto = ParticipationRequestDto.builder()
                .id(100L)
                .requester(1L)
                .event(10L)
                .status(ParticipationRequestStatus.CANCELED)
                .created(LocalDateTime.now())
                .build();
        when(eventRequestService.cancelRequest(eq(1L), eq(100L))).thenReturn(cancelledDto);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void cancelRequest_NotFound_ShouldReturn404() throws Exception {
        when(eventRequestService.cancelRequest(eq(1L), eq(999L)))
                .thenThrow(new NotFoundException("Заявка с id=999 не найдена"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelRequest_Conflict_ShouldReturn409() throws Exception {
        when(eventRequestService.cancelRequest(eq(1L), eq(100L)))
                .thenThrow(new ConflictException("Можно отменить только заявку в статусе PENDING"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 100L))
                .andExpect(status().isConflict());
    }
}
