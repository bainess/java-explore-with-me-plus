package ru.practicum.explorewithme.service.location.dal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.service.location.model.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByName(String name);

    Boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Boolean existsByNameIgnoreCase(String name);
}
