package ru.practicum.explorewithme.stats.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public final class EndpointHitRequestDTO {
    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    private final String app;

    @NotBlank(message = "URI не может быть пустым")
    private final String uri;

    @NotBlank(message = "ip не может быть пустым")
    String ip;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final @NotNull(message = "Дата и время, когда был совершен запрос к эндпоинту, должна быть указана")
    LocalDateTime timestamp;

}
