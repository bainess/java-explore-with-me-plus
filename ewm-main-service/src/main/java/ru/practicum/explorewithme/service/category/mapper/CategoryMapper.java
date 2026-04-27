package ru.practicum.explorewithme.service.category.mapper;

import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.model.Category;

public class CategoryMapper {

    public static Category mapToCategory(NewCategoryRequest request) {
        return new Category(
                null,
                request.getName()
        );
    }
}
