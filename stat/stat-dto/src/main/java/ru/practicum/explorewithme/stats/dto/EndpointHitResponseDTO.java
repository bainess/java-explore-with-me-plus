package ru.practicum.explorewithme.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class EndpointHitResponseDTO {
    private final String description;

    private static final String DEFAULT_DESCRIPTION = "Информация сохранена";

    public static EndpointHitResponseDTO createDefault() {
        return new EndpointHitResponseDTO(DEFAULT_DESCRIPTION);
    }
}