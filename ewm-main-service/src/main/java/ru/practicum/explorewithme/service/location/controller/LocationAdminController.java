package ru.practicum.explorewithme.service.location.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;
import ru.practicum.explorewithme.service.location.service.LocationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/locations")
public class LocationAdminController {
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<LocationDto> getAllLocations(@RequestParam(name = "from") Integer from,
                                             @RequestParam(name = "size") Integer size) {
        return locationService.getAllLocations(from, size);
    }

}
