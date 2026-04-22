package ru.practicum.explorewithme.stats.dto;

public record EndpointHitResponseDTO(
        String description
) {
    private static final String DEFAULT_DESCRIPTION = "Информация сохранена";

    public static EndpointHitResponseDTO createDefault() {
        return new EndpointHitResponseDTO(DEFAULT_DESCRIPTION);
    }
}