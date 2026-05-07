package ru.practicum.explorewithme.service.location.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;
import ru.practicum.explorewithme.service.location.model.AdminLocation;

@UtilityClass
public class LocationMapper {
    public static AdminLocation toEntity(NewLocationRequest request) {
        return AdminLocation.builder()
                .name(request.getName())
                .lat(request.getLat())
                .lon(request.getLon())
                .radius(request.getRadius())
                .build();
    }

    public static LocationDto toDto(AdminLocation location) {
        return LocationDto.builder()
                .id(location.getId())
                .name(location.getName())
                .lat(location.getLat())
                .lon(location.getLon())
                .radius(location.getRadius())
                .build();
    }

    public static void updateEntity(UpdateLocationRequest request, AdminLocation location) {
        if (request.getName() != null) {
            location.setName(request.getName());
        }
        if (request.getLat() != null) {
            location.setLat(request.getLat());
        }
        if (request.getLon() != null) {
            location.setLon(request.getLon());
        }
        if (request.getRadius() != null) {
            location.setRadius(request.getRadius());
        }
    }
}
