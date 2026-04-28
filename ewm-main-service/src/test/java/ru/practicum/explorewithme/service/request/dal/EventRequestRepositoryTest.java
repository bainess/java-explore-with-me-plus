package ru.practicum.explorewithme.service.request.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.model.ParticipationRequest;
import ru.practicum.explorewithme.service.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRequestRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private EventRequestRepository requestRepository;

    private User requester;
    private User initiator;
    private Event event;

    @BeforeEach
    void setUp() {
        initiator = new User(null, "initiator@example.com", "Initiator");
        em.persist(initiator);
        requester = new User(null, "requester@example.com", "Requester");
        em.persist(requester);

        event = new Event();
        event.setTitle("Event");
        event.setAnnotation("ann");
        event.setDescription("desc");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setLocation(new ru.practicum.explorewithme.service.event.model.Location(55f, 37f));
        event.setPaid(false);
        event.setParticipantLimit(5);
        event.setRequestModeration(true);
        event.setState(EventState.PUBLISHED);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(initiator);
        em.persist(event);
    }

    @Test
    void countByEventIdAndStatus_ReturnsCorrectCount() {
        ParticipationRequest req1 = createRequest(ParticipationRequestStatus.CONFIRMED);
        ParticipationRequest req2 = createRequest(ParticipationRequestStatus.CONFIRMED);
        createRequest(ParticipationRequestStatus.PENDING);

        Integer count = requestRepository.countByEventIdAndStatus(event.getId(), ParticipationRequestStatus.CONFIRMED);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findAllByEventIdAndEventInitiatorId_ReturnsRequests() {
        createRequest(ParticipationRequestStatus.PENDING);
        List<ParticipationRequest> result = requestRepository
                .findAllByEventIdAndEventInitiatorId(event.getId(), initiator.getId());
        assertThat(result).hasSize(1);
    }

    private ParticipationRequest createRequest(ParticipationRequestStatus status) {
        ParticipationRequest req = new ParticipationRequest();
        req.setCreated(LocalDateTime.now());
        req.setEvent(event);
        req.setRequester(requester);
        req.setStatus(status);
        return em.persist(req);
    }
}
