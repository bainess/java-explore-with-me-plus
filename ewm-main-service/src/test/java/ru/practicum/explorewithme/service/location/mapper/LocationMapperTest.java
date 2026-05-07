package ru.practicum.explorewithme.service.location.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;
import ru.practicum.explorewithme.service.location.model.AdminLocation;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    @Test
    void toEntity() {
        NewLocationRequest request = NewLocationRequest.builder()
                .name("Test Location")
                .lat(55.0f)
                .lon(37.0f)
                .radius(10.5f)
                .build();

        AdminLocation entity = LocationMapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getLat()).isEqualTo(request.getLat());
        assertThat(entity.getLon()).isEqualTo(request.getLon());
        assertThat(entity.getRadius()).isEqualTo(request.getRadius());
    }

    @Test
    void toDto() {
        AdminLocation entity = AdminLocation.builder()
                .id(1L)
                .name("Test Location")
                .lat(55.0f)
                .lon(37.0f)
                .radius(10.5f)
                .build();

        LocationDto dto = LocationMapper.toDto(entity);

        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getName()).isEqualTo(entity.getName());
        assertThat(dto.getLat()).isEqualTo(entity.getLat());
        assertThat(dto.getLon()).isEqualTo(entity.getLon());
        assertThat(dto.getRadius()).isEqualTo(entity.getRadius());
    }

    @Test
    void updateEntity() {
        AdminLocation entity = AdminLocation.builder()
                .id(1L)
                .name("Old Name")
                .lat(55.0f)
                .lon(37.0f)
                .radius(10.0f)
                .build();

        UpdateLocationRequest request = UpdateLocationRequest.builder()
                .name("New Name")
                .radius(15.0f)
                .build();

        LocationMapper.updateEntity(request, entity);

        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getRadius()).isEqualTo(15.0f);
        assertThat(entity.getLat()).isEqualTo(55.0f);
        assertThat(entity.getLon()).isEqualTo(37.0f);
    }
}
