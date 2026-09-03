package ru.practicum.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import java.io.Serializable;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ViewStatsDto implements Serializable {
    String app;
    String uri;
    Long hits;

    public ViewStatsDto(String app, String uri, Long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }
}
