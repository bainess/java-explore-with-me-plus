package ru.practicum.explorewithme.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ru.practicum.explorewithme.stats")
public class StatServerApp {
    public static void main(String[] args) {
        SpringApplication.run(StatServerApp.class, args);
    }
}
