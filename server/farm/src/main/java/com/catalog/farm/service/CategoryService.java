package com.catalog.farm.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.catalog.farm.dto.request.CategoryDTO;
import com.catalog.farm.dto.response.CategoryResponseDTO;
import com.catalog.farm.entity.Category;
import com.catalog.farm.exception.ResourceAlreadyExistsException;
import com.catalog.farm.exception.ResourceNotFoundException;
import com.catalog.farm.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDTO> getAllCategories() {
        return this.categoryRepository.findAll()
                .stream()
                .map(CategoryResponseDTO::from)
                .toList();
    }

    public CategoryResponseDTO getCategoryById(Long id) {
        return this.categoryRepository.findById(id)
                .map(CategoryResponseDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public CategoryResponseDTO createCategory(CategoryDTO req) {
        Category existingCategory = this.categoryRepository.findOneByName(req.name());
        if (existingCategory != null) {
            throw new ResourceAlreadyExistsException("Category with name '" + req.name() + "' already exists.");
        }

        Category category = new Category();
        category.setName(req.name());
        category.setDescription(req.description());

        return CategoryResponseDTO.from(this.categoryRepository.save(category));
    }

    public CategoryResponseDTO updateCategory(Long id, CategoryDTO req) {
        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Category withSameName = this.categoryRepository.findOneByName(req.name());
        if (withSameName != null && !withSameName.getId().equals(id)) {
            throw new ResourceAlreadyExistsException("Category with name '" + req.name() + "' already exists.");
        }

        category.setName(req.name());
        category.setDescription(req.description());

        return CategoryResponseDTO.from(this.categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        this.categoryRepository.delete(category);
    }
}
