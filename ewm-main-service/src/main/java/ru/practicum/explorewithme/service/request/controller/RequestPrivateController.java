package ru.practicum.explorewithme.service.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.service.EventRequestService;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestPrivateController {

    private final EventRequestService eventRequestService;

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        return eventRequestService.getUserRequests(userId);
    }

    @PostMapping
    public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                                          @RequestParam Long eventId) {
        return eventRequestService.addParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                @PathVariable Long requestId) {
        return eventRequestService.cancelRequest(userId, requestId);
    }
}
