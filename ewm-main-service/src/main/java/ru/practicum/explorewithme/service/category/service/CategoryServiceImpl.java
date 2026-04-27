package ru.practicum.explorewithme.service.category.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.service.category.dal.CategoryRepository;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.exception.DuplicatedDataException;

import java.util.zip.DataFormatException;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService{
    private CategoryRepository categoryRepository;

    public Category createCategory(NewCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {throw new DuplicatedDataException("Категория " +
                request.getName() + " уже существует");
        }
        Category category = CategoryMapper.mapToCategory(request);
        return categoryRepository.save(category);
    }
}
