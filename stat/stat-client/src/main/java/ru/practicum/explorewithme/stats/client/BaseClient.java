package ru.practicum.explorewithme.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
public abstract class BaseClient {
    protected final WebClient webClient;

    protected BaseClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected <T> void post(String path, T body) {
        try {
            webClient.post()
                    .uri(path)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();
        } catch (Exception e) {
            log.warn("Не удалось сохранить статистику: {}", e.getMessage());
        }
    }

    protected ResponseEntity<Object> get(String path, @Nullable Map<String, Object> parameters) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    if (parameters != null) {
                        parameters.forEach(uriBuilder::queryParam);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .toEntity(Object.class)
                .onErrorResume(e -> Mono.empty()) // Базовая обработка ошибок
                .block();
    }
}
