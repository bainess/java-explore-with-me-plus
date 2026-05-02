package ru.practicum.explorewithme.service.request.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface EventRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByEventIdAndEventInitiatorId(Long eventId, Long initiatorId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    Long countByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    List<ParticipationRequest> findAllByIdInAndStatus(List<Long> ids, ParticipationRequestStatus status);
}
