package ru.practicum.explorewithme.service.location.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.explorewithme.service.event.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.service.LocationService;

@Service
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/location")
public class AdminLocationController {
    private final LocationService locationService;

    @PostMapping
    public LocationDto saveLocation(@RequestBody NewLocationRequest request) {
        locationService.createLocation(request);
    }
}
