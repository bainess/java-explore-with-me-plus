package ru.practicum.explorewithme.service.location.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;
import ru.practicum.explorewithme.service.location.service.LocationService;

@Service
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/location")
public class AdminLocationController {
    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDto saveLocation(@RequestBody NewLocationRequest request) {
        log.info("Запрос на сохранение новой локации lat - {}, lon - {}, rad - {}",
                request.getLat(), request.getLon(), request.getRadius());
        return locationService.createLocation(request);
    }

    @PatchMapping("/{locId}")
    @ResponseStatus(HttpStatus.OK)
    public LocationDto updateLocation(@PathVariable(name = "locId") Long locId,
                                      @RequestBody UpdateLocationRequest request) {
        log.info("Запрос на обновление локации lat - {}, lon - {}, rad - {}",
                request.getLat(), request.getLon(), request.getRadius());
        return locationService.updateLocation(locId, request);
    }

    @DeleteMapping("/{locId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeLocation(@PathVariable(name = "locId") Long locId) {
        log.info("Запрос на удаление локации id {}", locId);
        locationService.deleteLocation(locId);
    }

    @GetMapping("/{locId}")
    @ResponseStatus(HttpStatus.OK)
    public LocationDto getLocation(@PathVariable(name = "locId") Long locId) {
        log.info("Запрос на получение локации id {}", locId);
        return locationService.getLocationById(locId);
    }

}
