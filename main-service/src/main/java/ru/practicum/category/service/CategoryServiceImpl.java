package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        log.info("Getting categories: from={}, size={}", from, size);

        Pageable pageable = PageRequest.of(
                from / size,
                size
        );

        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoriesById(Long catId) {
        log.info("Getting category with id={}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Category with id=" + catId + " was not found")
                );

        return categoryMapper.toDto(category);
    }

    @Override
    public void deleteCategory(Long catId) {
        log.info("Deleting category with id={}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Category with id=" + catId + " was not found"
                        )
                );

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.delete(category);

        log.info("Category deleted successfully: id={}", catId);
    }

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Creating category: name={}", newCategoryDto.getName());

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException(
                    "Category with name=" + newCategoryDto.getName() + " already exists"
            );
        }

        Category category = categoryMapper.toEntity(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully: id={}", savedCategory.getId());

        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Updating category: id={}, name={}", catId, categoryDto.getName());

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Category with id=" + catId + " was not found"
                        )
                );

        if (categoryRepository.existsByNameAndIdNot(categoryDto.getName(), catId)) {
            throw new ConflictException(
                    "Category with name=" + categoryDto.getName() + " already exists"
            );
        }

        category.setName(categoryDto.getName());

        Category savedCategory = categoryRepository.save(category);

        log.info("Category updated successfully: id={}", savedCategory.getId());

        return categoryMapper.toDto(savedCategory);
    }
}
