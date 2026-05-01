package ru.practicum.explorewithme.service.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.request.dal.EventRequestRepository;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.service.request.model.ParticipationRequest;
import ru.practicum.explorewithme.service.user.dal.UserRepository;
import ru.practicum.explorewithme.service.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPrivateServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventRequestRepository eventRequestRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventRequestServiceImpl requestService;

    private User user;
    private Event event;

    private User initiator;
    private User requester;

    @BeforeEach
    void setUp() {
        initiator = new User();
        initiator.setId(2L);

        requester = new User();
        requester.setId(1L);

        event = new Event();
        event.setId(10L);
        event.setInitiator(initiator);
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(0);
        event.setRequestModeration(true);
    }

    // Тест-кейсы для getUserRequests

    @Test
    void getUserRequests_Success() {
        ParticipationRequest request = ParticipationRequest.builder()
                .id(100L)
                .requester(requester)
                .event(event)
                .status(ParticipationRequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(eventRequestRepository.findAllByRequesterId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = requestService.getUserRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        assertThat(result.get(0).getStatus()).isEqualTo(ParticipationRequestStatus.CONFIRMED);
    }

    @Test
    void getUserRequests_EmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(eventRequestRepository.findAllByRequesterId(1L)).thenReturn(List.of());

        List<ParticipationRequestDto> result = requestService.getUserRequests(1L);

        assertThat(result).isEmpty();
    }

    // Тест-кейсы для addParticipationRequest

    @Test
    void addParticipationRequest_SuccessPending() {
        event.setParticipantLimit(10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRequestRepository.existsByEventIdAndRequesterId(10L, 1L)).thenReturn(false);
        when(eventRequestRepository.countByEventIdAndStatus(10L, ParticipationRequestStatus.CONFIRMED)).thenReturn(0);
        when(eventRequestRepository.save(any(ParticipationRequest.class))).thenAnswer(inv -> {
            ParticipationRequest r = inv.getArgument(0);
            r.setId(200L);
            return r;
        });

        ParticipationRequestDto result = requestService.addParticipationRequest(1L, 10L);

        assertThat(result.getId()).isEqualTo(200L);
        assertThat(result.getStatus()).isEqualTo(ParticipationRequestStatus.PENDING);
    }

    @Test
    void addParticipationRequest_SuccessConfirmedWhenModerationFalse() {
        event.setRequestModeration(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRequestRepository.existsByEventIdAndRequesterId(10L, 1L)).thenReturn(false);
        when(eventRequestRepository.save(any(ParticipationRequest.class))).thenAnswer(inv -> {
            ParticipationRequest r = inv.getArgument(0);
            r.setId(201L);
            return r;
        });

        ParticipationRequestDto result = requestService.addParticipationRequest(1L, 10L);

        assertThat(result.getStatus()).isEqualTo(ParticipationRequestStatus.CONFIRMED);
    }

    @Test
    void addParticipationRequest_SuccessConfirmedWhenLimitZero() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRequestRepository.existsByEventIdAndRequesterId(10L, 1L)).thenReturn(false);
        when(eventRequestRepository.save(any(ParticipationRequest.class))).thenAnswer(inv -> {
            ParticipationRequest r = inv.getArgument(0);
            r.setId(202L);
            return r;
        });

        ParticipationRequestDto result = requestService.addParticipationRequest(1L, 10L);

        assertThat(result.getStatus()).isEqualTo(ParticipationRequestStatus.CONFIRMED);
    }

    @Test
    void addParticipationRequest_ConflictWhenEventNotPublished() {
        event.setState(EventState.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> requestService.addParticipationRequest(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("неопубликованное событие");
    }

    @Test
    void addParticipationRequest_ConflictWhenUserIsInitiator() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> requestService.addParticipationRequest(2L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("не может подать заявку на свое событие");
    }

    @Test
    void addParticipationRequest_ConflictWhenAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRequestRepository.existsByEventIdAndRequesterId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> requestService.addParticipationRequest(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Заявка на это событие уже подана");
    }

    @Test
    void addParticipationRequest_ConflictWhenLimitReached() {
        event.setParticipantLimit(2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRequestRepository.existsByEventIdAndRequesterId(10L, 1L)).thenReturn(false);
        when(eventRequestRepository.countByEventIdAndStatus(10L, ParticipationRequestStatus.CONFIRMED)).thenReturn(2);

        assertThatThrownBy(() -> requestService.addParticipationRequest(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Лимит участников исчерпан");
    }

    // Тест-кейсы для cancelRequest

    @Test
    void cancelRequest_Success() {
        ParticipationRequest request = ParticipationRequest.builder()
                .id(300L)
                .requester(requester)
                .event(event)
                .status(ParticipationRequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        when(eventRequestRepository.findByIdAndRequesterId(300L, 1L)).thenReturn(Optional.of(request));
        when(eventRequestRepository.save(any(ParticipationRequest.class))).thenReturn(request);

        ParticipationRequestDto result = requestService.cancelRequest(1L, 300L);

        assertThat(result.getStatus()).isEqualTo(ParticipationRequestStatus.CANCELED);
    }

    @Test
    void cancelRequest_NotFound() {
        when(eventRequestRepository.findByIdAndRequesterId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.cancelRequest(1L, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Заявка с id=999 не найдена");
    }

    @Test
    void cancelRequest_ConflictWhenNotPending() {
        ParticipationRequest request = ParticipationRequest.builder()
                .id(301L)
                .requester(user)
                .event(event)
                .status(ParticipationRequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();

        when(eventRequestRepository.findByIdAndRequesterId(301L, 1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.cancelRequest(1L, 301L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Можно отменить только заявку в статусе PENDING");
    }
}
