package ru.practicum.explorewithme.service.category.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.dto.UpdateCategoryRequest;
import ru.practicum.explorewithme.service.category.service.CategoryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody NewCategoryRequest request) {
        log.info("Получен запрос на создание категории {}", request.getName());

        return categoryService.createCategory(request);
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto createCategory(@PathVariable("catId") Long catId,
                                      @RequestBody UpdateCategoryRequest categoryRequest) {
        log.info("Запрос на изменение категории {}", catId);
        return categoryService.changeCategory(catId, categoryRequest);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCategory(@PathVariable("catId") Long catId) {
        log.info("Запрос на удаление категории {}", catId);
        categoryService.removeCategory(catId);
    }
}
