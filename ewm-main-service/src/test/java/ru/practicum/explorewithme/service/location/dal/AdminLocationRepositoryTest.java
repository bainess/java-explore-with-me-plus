package ru.practicum.explorewithme.service.location.dal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.explorewithme.service.location.model.AdminLocation;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AdminLocationRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AdminLocationRepository repository;

    @Test
    void saveAndFind() {
        AdminLocation loc = AdminLocation.builder()
                .name("Парк Горького")
                .lat(55.72f)
                .lon(37.60f)
                .radius(1.5f)
                .build();

        AdminLocation saved = repository.save(loc);
        assertThat(saved.getId()).isNotNull();

        AdminLocation found = repository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Парк Горького");
    }

    @Test
    void existsByName() {
        AdminLocation loc = AdminLocation.builder()
                .name("ВДНХ")
                .lat(55.82f)
                .lon(37.63f)
                .radius(2.0f)
                .build();
        em.persist(loc);

        assertThat(repository.existsByName("ВДНХ")).isTrue();
        assertThat(repository.existsByName("Лужники")).isFalse();
    }
}
