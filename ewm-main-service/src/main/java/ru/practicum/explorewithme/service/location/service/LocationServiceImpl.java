package ru.practicum.explorewithme.service.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.location.dal.LocationRepository;
import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;
import ru.practicum.explorewithme.service.location.mapper.LocationMapper;
import ru.practicum.explorewithme.service.location.model.Location;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    @Transactional
    @Override
    public LocationDto createLocation(NewLocationRequest request) {
        Location location = LocationMapper.toEntity(request);
        location = locationRepository.save(location);
        return LocationMapper.toDto(location);
    }

    @Transactional
    @Override
    public LocationDto updateLocation(Long locId, UpdateLocationRequest request) {
        Location location = locationRepository.findById(locId).orElseThrow(() -> new NotFoundException("Локация" + locId +" не найдена"));
        LocationMapper.updateEntity(request, location);
        location = locationRepository.save(location);
        return LocationMapper.toDto(location);
    }

    @Override
    public void deleteLocation(Long locId) {
        Location location = locationRepository.findById(locId).orElseThrow(() -> new NotFoundException("Локация" + locId +" не найдена"));
        locationRepository.delete(location);
    }

    @Override
    public LocationDto getLocationById(Long locId) {
        return null;
    }

    @Override
    public List<LocationDto> getAllLocations(int from, int size) {
        return List.of();
    }

}
