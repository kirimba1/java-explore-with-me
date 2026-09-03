package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

@Component
public class StatsClient {

    private final RestClient restClient;

    public StatsClient(@Value("${stat-service.url}") String statServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(statServiceUrl).build();
    }

    public void sendHit(EndpointHitDto hitDto) {
        restClient.post()
                .uri("/hit")
                .body(hitDto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStats(String start, String end) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/stats")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
