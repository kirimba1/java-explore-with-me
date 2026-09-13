package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationStatus;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getRequestPrivate(Long userId) {
        log.info("Getting requests for user with id={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User with id=" + userId + " was not found"));

        return requestRepository.findAllByUserId(userId);
    }

    @Override
    public ParticipationRequestDto addRequestPrivate(Long userId, Long eventId) {
        log.info("Creating request: userId={}, eventId={}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Event with id=" + eventId + " was not found"));

        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Initiator cannot request own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }

        if (requestRepository.findByEventIdAndUserId(eventId, userId).isPresent()) {
            throw new ConflictException("Request already exists");
        }

        if (event.getParticipantLimit() > 0
                && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }

        Request request = new Request();
        request.setUser(user);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());

        if (!event.getRequestModeration()) {
            request.setStatus(ParticipationStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        } else {
            request.setStatus(ParticipationStatus.PENDING);
        }

        Request savedRequest = requestRepository.save(request);

        log.info("Request created successfully: id={}", savedRequest.getId());

        return requestMapper.toDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancelRequestPrivate(Long userId, Long requestId) {
        log.info("Canceling request: userId={}, requestId={}", userId, requestId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getUser().getId().equals(userId)) {
            throw new NotFoundException(
                    "Request with id=" + requestId + " was not found");
        }

        request.setStatus(ParticipationStatus.CANCELED);

        Request savedRequest = requestRepository.save(request);

        log.info("Request canceled successfully: id={}", savedRequest.getId());

        return requestMapper.toDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for event: userId={}, eventId={}", userId, eventId);

        findOwnedEvent(userId, eventId);

        return requestRepository.findByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(
            Long userId, Long eventId, EventRequestStatusUpdateRequest dto
    ) {
        log.info("Updating request statuses: userId={}, eventId={}", userId, eventId);

        Event event = findOwnedEvent(userId, eventId);

        List<Request> requests = requestRepository.findByIdIn(dto.getRequestIds());

        for (Request request : requests) {
            if (request.getStatus() != ParticipationStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        boolean moderationNotRequired = event.getParticipantLimit() == 0
                || Boolean.FALSE.equals(event.getRequestModeration());

        if (moderationNotRequired || dto.getStatus() == ParticipationStatus.REJECTED) {
            ParticipationStatus targetStatus = moderationNotRequired
                    ? ParticipationStatus.CONFIRMED
                    : ParticipationStatus.REJECTED;

            for (Request request : requests) {
                request.setStatus(targetStatus);
                (targetStatus == ParticipationStatus.CONFIRMED ? confirmed : rejected)
                        .add(requestMapper.toDto(request));
            }
            requestRepository.saveAll(requests);
            return new EventRequestStatusUpdateResult(confirmed, rejected);
        }

        for (Request request : requests) {
            if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("The participant limit has been reached");
            }
            request.setStatus(ParticipationStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            confirmed.add(requestMapper.toDto(request));
        }

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            List<Request> pending = requestRepository.findByEventId(eventId).stream()
                    .filter(r -> r.getStatus() == ParticipationStatus.PENDING)
                    .toList();
            for (Request request : pending) {
                request.setStatus(ParticipationStatus.REJECTED);
                rejected.add(requestMapper.toDto(request));
            }
            requestRepository.saveAll(pending);
        }

        requestRepository.saveAll(requests);
        eventRepository.save(event);

        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    private Event findOwnedEvent(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id=" + eventId + " was not found"));
    }
}
