package ru.practicum.event.mapper;

import org.mapstruct.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "lat", ignore = true)
    @Mapping(target = "lon", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event toEntity(NewEventDto newEventDto);

    EventShortDto toShortDto(Event event, Integer views, Integer confirmedRequest);

    @Mapping(target = "location", ignore = true)
    EventFullDto toFullDto(Event event, Integer views, Integer confirmedRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "lat", ignore = true)
    @Mapping(target = "lon", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    void updateEventFromDto(UpdateEventAdminRequestDto dto, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "lat", ignore = true)
    @Mapping(target = "lon", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    void updateEventFromUserDto(UpdateEventUserRequestDto dto, @MappingTarget Event event);
}
