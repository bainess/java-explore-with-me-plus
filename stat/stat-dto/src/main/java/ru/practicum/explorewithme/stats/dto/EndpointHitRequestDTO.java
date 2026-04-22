package ru.practicum.explorewithme.stats.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record EndpointHitRequestDTO(
        @NotBlank(message = "Идентификатор сервиса не может быть пустым")
        String app,

        @NotBlank(message = "URI не может быть пустым")
        String uri,

        @NotBlank(message = "ip не может быть пустым")
        String ip,

        @NotNull(message = "Дата и время, когда был совершен запрос к эндпоинту, должна быть указана")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {

}
