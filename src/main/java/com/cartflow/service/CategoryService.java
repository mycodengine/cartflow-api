package com.cartflow.service;

import com.cartflow.domain.entity.Category;
import com.cartflow.dto.request.CategoryRequest;
import com.cartflow.dto.response.CategoryResponse;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.mapper.CategoryMapper;
import com.cartflow.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toResponse).toList();
    }

    public CategoryResponse findById(Long id) {
        return categoryMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessException("Category already exists: " + request.name());
        }
        Category saved = categoryRepository.save(Category.builder()
                .name(request.name())
                .description(request.description())
                .slug(request.slug())
                .build());
        log.info("Category created: {}", saved.getName());
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getOrThrow(id);
        category.setName(request.name());
        category.setDescription(request.description());
        category.setSlug(request.slug());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(getOrThrow(id));
        log.info("Category {} deleted", id);
    }

    private Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}
