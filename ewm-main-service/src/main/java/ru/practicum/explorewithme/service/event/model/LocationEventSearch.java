package ru.practicum.explorewithme.service.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LocationEventSearch {
    private float lat;
    private float lon;
    private float radius;
}
