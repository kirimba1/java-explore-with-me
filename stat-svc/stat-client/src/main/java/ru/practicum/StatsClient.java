package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StatsClient {

    private final RestClient restClient;
    private final String appName;

    public StatsClient(@Value("${stat-service.url}") String statServiceUrl,
                       @Value("${stat-service.app-name}") String appName) {
        this.restClient = RestClient.builder().baseUrl(statServiceUrl).build();
        this.appName = appName;
    }

    public void sendHit(String uri, String ip) {
        restClient.post()
                .uri("/hit")
                .body(new EndpointHitDto(appName, uri, ip, LocalDateTime.now()))
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

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("uris", uris)
                        .queryParam("unique", unique)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                      }
                );
    }
}