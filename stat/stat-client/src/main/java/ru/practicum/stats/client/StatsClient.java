package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.stats.dto.EndpointHitDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> saveHit(EndpointHitDTO hitDto) {
        return post("/hit", hitDto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        StringBuilder pathBuilder = new StringBuilder("/stats?start={start}&end={end}");
        Map<String, Object> parameters = new java.util.HashMap<>(Map.of(
                "start", start.format(FORMATTER),
                "end", end.format(FORMATTER)
        ));

        if (uris != null && !uris.isEmpty()) {
            pathBuilder.append("&uris={uris}");
            parameters.put("uris", String.join(",", uris));
        }

        if (unique != null) {
            pathBuilder.append("&unique={unique}");
            parameters.put("unique", unique);
        }

        return get(pathBuilder.toString(), parameters);
    }
}
