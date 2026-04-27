package ru.practicum.explorewithme.service.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.category.service.CategoryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@RequestBody NewCategoryRequest request) {
        log.info("Получен запрос на создание категории {}", request.getName());

        return categoryService.createCategory(request);
    };
}
