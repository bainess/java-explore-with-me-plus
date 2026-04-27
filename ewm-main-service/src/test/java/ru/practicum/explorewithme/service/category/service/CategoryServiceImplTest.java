package ru.practicum.explorewithme.service.category.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.service.category.dal.CategoryRepository;
import ru.practicum.explorewithme.service.category.dto.NewCategoryRequest;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.exception.DuplicatedDataException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    NewCategoryRequest request;
    Category category;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        request = new NewCategoryRequest("new category");
        category = new Category(1L, "new category");
    }

//    сохраняет новую категорию

    @Test
    void shouldSaveCategory() {
        Mockito.when(categoryRepository.existsByName(Mockito.anyString()))
                        .thenReturn(false);

        Mockito.when(categoryRepository.save(Mockito.any()))
                .thenReturn(category);

        Category addedCategory = categoryService.createCategory(request);
        Assertions.assertEquals(category.getName(), addedCategory.getName());
    }

//    бросает ошибку, когда данные повторяются
    @Test
    void shouldThrowExceptionWhenCategoryDuplicated() {
        Mockito.when(categoryRepository.existsByName(Mockito.anyString()))
                .thenReturn(true);

        assertThrows(DuplicatedDataException.class, () -> categoryService.createCategory(request));

        verify(categoryRepository, times(1)).existsByName(request.getName());
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
