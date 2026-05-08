package ru.practicum.explorewithme.service.location.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.service.location.model.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByName(String name);

    Optional<Location> findByNameIgnoreCaseAndIdNot(String name, Long id);
}
