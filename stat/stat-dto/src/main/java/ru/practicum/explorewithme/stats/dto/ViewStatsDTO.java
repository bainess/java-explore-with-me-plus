package ru.practicum.explorewithme.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ViewStatsDTO {
    private final String app;
    private final String uri;
    private final Long hits;
}
