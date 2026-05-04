package ru.practicum.explorewithme.service.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.service.EventRequestService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserRequestController {
    private final EventRequestService requestService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto saveEventParticipation(@PathVariable(name = "userId") Long userId,
                                                          @RequestParam(name = "eventId", required = true) Long eventId) {
        return requestService.saveEventParticipation(userId, eventId);
    }

}
