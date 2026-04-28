package ru.practicum.explorewithme.service.category.controller;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.category.service.CategoryService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryPublicController {
    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> createCategory(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @PositiveOrZero @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Запрос на получение категорий с {} размером {}", from, size);
        return categoryService.getAllCategories(from, size);
    }
}
