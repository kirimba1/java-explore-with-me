package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "event", ignore = true)
    ParticipationRequestDto toDto(Request request);
}
