package ru.practicum.explorewithme.service.location.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.service.location.model.AdminLocation;

import java.util.Optional;

public interface AdminLocationRepository extends JpaRepository<AdminLocation, Long> {
    boolean existsByName(String name);

    Optional<AdminLocation> findByNameIgnoreCaseAndIdNot(String name, Long id);
}
