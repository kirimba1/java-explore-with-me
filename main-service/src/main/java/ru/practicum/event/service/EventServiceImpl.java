package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String APP_NAME = "ewm-main-service";
    private static final LocalDateTime STATS_EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final EventViewsService eventViewsService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public List<EventShortDto> getEvents(
            String text, List<Long> categories, Boolean paid,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
            String sort, Integer from, Integer size, String ip
    ) {
        log.info("Getting public events");

        if (rangeStart != null
                && rangeEnd != null
                && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }

        if (sort != null
                && !"VIEWS".equals(sort)
                && !"EVENT_DATE".equals(sort)) {
            throw new BadRequestException("Unknown sort: " + sort);
        }

        text = text == null ? "" : text;

        if (categories != null && categories.isEmpty()) {
            categories = null;
        }

        List<Event> events = eventRepository.findPublicEvents(
                EventState.PUBLISHED,
                text.toLowerCase(),
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable
        );

        sendHit("/events", ip);

        if (events.isEmpty()) {
            return List.of();
        }

        Map<String, Long> views = eventViewsService.getViewsMap(
                toUris(events),
                rangeStart,
                rangeEnd
        );

        if ("VIEWS".equals(sort)) {
            events.sort(Comparator.comparing(
                    event -> viewsFor(event, views),
                    Comparator.reverseOrder()
            ));
        } else if ("EVENT_DATE".equals(sort)) {
            events.sort(Comparator.comparing(Event::getEventDate));
        }

        return paginateEvents(events, from, size).stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        viewsFor(event, views).intValue(),
                        event.getConfirmedRequests()
                ))
                .toList();
    }

    @Override
    public EventFullDto getEventsById(Long eventId, String ip) {
        log.info("Getting published event: id={}", eventId);

        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> notFound(eventId));

        String uri = eventUri(eventId);
        sendHit(uri, ip);

        int views = eventViewsService.getViewsMap(List.of(uri), event.getCreatedOn(), null)
                .getOrDefault(uri, 0L).intValue();

        EventFullDto eventFullDto = eventMapper.toFullDto(event, views, event.getConfirmedRequests());

        log.info("Event retrieved successfully: id={}, views={}", eventId, views);

        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getAdminEvents(
            List<Long> users, List<String> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Integer from, Integer size
    ) {
        log.info("Getting admin events");

        boolean usersEmpty = users == null || users.isEmpty();
        boolean statesEmpty = states == null || states.isEmpty();
        boolean categoriesEmpty = categories == null || categories.isEmpty();

        List<Long> userIds = usersEmpty ? List.of(-1L) : users;
        List<EventState> eventStates = statesEmpty
                ? List.of(EventState.PENDING)
                : states.stream().map(EventState::valueOf).toList();
        List<Long> categoryIds = categoriesEmpty ? List.of(-1L) : categories;

        List<Event> events = eventRepository.findAdminEvents(
                userIds, eventStates, categoryIds, rangeStart,
                rangeEnd, usersEmpty, statesEmpty, categoriesEmpty
        );
        if (events.isEmpty()) {
            return List.of();
        }

        Map<String, Long> views = eventViewsService.getViewsMap(toUris(events), rangeStart, rangeEnd);

        return paginateEvents(events, from, size).stream()
                .map(event -> eventMapper.toFullDto(
                        event, viewsFor(event, views).intValue(), event.getConfirmedRequests()
                ))
                .toList();
    }

    @Override
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequestDto dto) {
        log.info("Updating event: id={}", eventId);

        Event event = eventRepository.findById(eventId).orElseThrow(() -> notFound(eventId));

        if (dto.getEventDate() != null
                && event.getPublishedOn() != null
                && dto.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
            throw new ConflictException(
                    "Event date must be at least one hour after publication");
        }

        eventMapper.updateEventFromDto(dto, event);
        applyStateAction(dto, event);
        applyLocation(dto.getLocation(), event);

        if (dto.getCategory() != null) {
            event.setCategory(findCategory(dto.getCategory()));
        }

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toFullDto(savedEvent, 0, savedEvent.getConfirmedRequests());
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Getting events for user: id={}", userId);

        findUser(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        Map<String, Long> views = eventViewsService.getViewsMap(toUris(events), STATS_EPOCH, null);

        return events.stream()
                .map(event -> eventMapper.toShortDto(
                        event, viewsFor(event, views).intValue(), event.getConfirmedRequests()
                ))
                .toList();
    }

    @Override
    public EventFullDto addUserEvents(Long userId, NewEventDto dto) {
        log.info("Creating event for user: id={}", userId);

        validateEventDateFromNow(dto.getEventDate());

        User initiator = findUser(userId);
        Category category = findCategory(dto.getCategory());

        Event event = eventMapper.toEntity(dto);
        event.setInitiator(initiator);
        event.setCategory(category);
        applyLocation(dto.getLocation(), event);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0);
        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);

        Event savedEvent = eventRepository.save(event);

        log.info("Event created: id={}", savedEvent.getId());

        return eventMapper.toFullDto(savedEvent, 0, savedEvent.getConfirmedRequests());
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        log.info("Getting event: userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> notFound(eventId));

        String uri = eventUri(eventId);
        int views = eventViewsService.getViewsMap(List.of(uri), event.getCreatedOn(), null)
                .getOrDefault(uri, 0L).intValue();

        return eventMapper.toFullDto(event, views, event.getConfirmedRequests());
    }

    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequestDto dto) {
        log.info("Updating event by owner: userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> notFound(eventId));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (dto.getEventDate() != null) {
            validateEventDateFromNow(dto.getEventDate());
        }

        eventMapper.updateEventFromUserDto(dto, event);

        if (dto.getStateAction() == StateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        } else if (dto.getStateAction() == StateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        applyLocation(dto.getLocation(), event);

        if (dto.getCategory() != null) {
            event.setCategory(findCategory(dto.getCategory()));
        }

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toFullDto(savedEvent, 0, savedEvent.getConfirmedRequests());
    }

    private void applyStateAction(UpdateEventAdminRequestDto dto, Event event) {
        if (dto.getStateAction() == StateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
            }
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Event date must be at least one hour after publication");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if (dto.getStateAction() == StateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot reject a published event");
            }
            event.setState(EventState.CANCELED);
        }
    }

    private void applyLocation(LocationDto location, Event event) {
        if (location != null) {
            event.setLocation(eventMapper.toEntity(location));
        }
    }

    private void validateEventDateFromNow(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException(
                    "Event date must be at least 2 hours after the current moment");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id=" + userId + " was not found"));
    }

    private Category findCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId.longValue())
                .orElseThrow(() -> new NotFoundException(
                        "Category with id=" + categoryId + " was not found"));
    }

    private NotFoundException notFound(Long eventId) {
        return new NotFoundException("Event with id=" + eventId + " was not found");
    }

    private String eventUri(Long eventId) {
        return "/events/" + eventId;
    }

    private List<String> toUris(List<Event> events) {
        return events.stream().map(event -> eventUri(event.getId())).toList();
    }

    private Long viewsFor(Event event, Map<String, Long> views) {
        return views.getOrDefault(eventUri(event.getId()), 0L);
    }

    private List<Event> paginateEvents(List<Event> events, Integer from, Integer size) {
        if (from >= events.size()) {
            return List.of();
        }
        int end = Math.min(from + size, events.size());
        return events.subList(from, end);
    }

    private void sendHit(String uri, String ip) {
        statsClient.sendHit(new EndpointHitDto(null, APP_NAME, uri, ip, LocalDateTime.now()));
    }
}