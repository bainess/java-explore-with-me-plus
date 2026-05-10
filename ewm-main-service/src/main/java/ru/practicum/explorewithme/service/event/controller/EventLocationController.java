package ru.practicum.explorewithme.service.event.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.service.event.dto.EventShortDto;
import ru.practicum.explorewithme.service.event.model.LocationEventSearch;
import ru.practicum.explorewithme.service.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/locations/events")
public class EventLocationController {
    private EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsByLocation(@RequestParam(name = "latitude") float lat,
                                                   @RequestParam(name = "longitude") float lon,
                                                   @RequestParam(name = "radius") float radius) {
        LocationEventSearch location = LocationEventSearch.builder().lat(lat).lon(lon).radius(radius).build();
        return eventService.findEventsByLocation(location);


    }
}
