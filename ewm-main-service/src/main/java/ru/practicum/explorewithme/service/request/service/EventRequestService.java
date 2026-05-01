package ru.practicum.explorewithme.service.request.service;

import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventRequestService {
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                     EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
