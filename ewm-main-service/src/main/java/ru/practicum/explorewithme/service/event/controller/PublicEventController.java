package ru.practicum.explorewithme.service.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.EventSearchParams;
import ru.practicum.explorewithme.service.event.dto.EventShortDto;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.service.exception.BadRequestException;
import ru.practicum.explorewithme.stats.client.StatsClient;
import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime rangeStart,
            @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime rangeEnd,
            @RequestParam (required = false) Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam (defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        if (rangeEnd != null && rangeStart != null && rangeEnd.isBefore(rangeStart)) {throw new BadRequestException("rangeEnd должен быть позже rangeStart");

        }
        try {
            statsClient.saveHit(EndpointHitDTO.builder()
                    .app("ewm-main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Не удалось сохранить статистику: {}", e.getMessage());
        }
        EventSearchParams params = EventSearchParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeEnd)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();
        return eventService.getEvents(params);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        try {
            statsClient.saveHit(EndpointHitDTO.builder()
                    .app("ewm-main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Не удалось сохранить статистику: {}", e.getMessage());
        }
        return eventService.getPublishedEvent(id);
    }
}
