package ru.practicum.explorewithme.service.category.service;

import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.model.Category;

public interface CategoryService {
    Category createCategory(NewCategoryRequest request);
}
