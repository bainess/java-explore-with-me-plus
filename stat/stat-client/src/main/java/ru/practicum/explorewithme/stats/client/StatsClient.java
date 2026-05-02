package ru.practicum.explorewithme.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class StatsClient extends BaseClient {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-service.url}") String serverUrl, WebClient.Builder builder) {
        super(builder.baseUrl(serverUrl).build());
    }

    public void saveHit(EndpointHitDTO hitDto) {
        try {
            post("/hit", hitDto);
        } catch (Exception e) {
            log.warn("Не удалось сохранить статистику: {}", e.getMessage());
        }
    }

    public List<ViewStatsDTO> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/stats")
                                .queryParam("start", start.format(FORMATTER))
                                .queryParam("end", end.format(FORMATTER));
                        if (uris != null && !uris.isEmpty()) {
                            uriBuilder.queryParam("uris", uris);
                        }
                        if (unique != null) {
                            uriBuilder.queryParam("unique", unique);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToFlux(ViewStatsDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.warn("Не удалось получить статистику: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
