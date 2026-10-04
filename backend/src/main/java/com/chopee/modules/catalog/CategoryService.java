package com.chopee.modules.catalog;

import com.chopee.entity.Category;
import com.chopee.modules.catalog.dto.CategoryTreeResponse;
import com.chopee.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderByDisplayOrderAsc();
        return rootCategories.stream()
                .map(this::mapToTreeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryTreeResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục với ID: " + id));
        return mapToTreeResponse(category);
    }

    @Transactional(readOnly = true)
    public List<Long> getCategoryAndDescendantIds(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục với ID: " + categoryId));

        List<Long> ids = new ArrayList<>();
        collectDescendantIds(category, ids);
        return ids;
    }

    private void collectDescendantIds(Category category, List<Long> ids) {
        ids.add(category.getId());
        if (category.getSubCategories() != null) {
            for (Category child : category.getSubCategories()) {
                collectDescendantIds(child, ids);
            }
        }
    }

    private CategoryTreeResponse mapToTreeResponse(Category category) {
        List<CategoryTreeResponse> children = new ArrayList<>();
        if (category.getSubCategories() != null) {
            children = category.getSubCategories().stream()
                    .sorted((c1, c2) -> Integer.compare(
                            c1.getDisplayOrder() != null ? c1.getDisplayOrder() : 0,
                            c2.getDisplayOrder() != null ? c2.getDisplayOrder() : 0
                    ))
                    .map(this::mapToTreeResponse)
                    .collect(Collectors.toList());
        }

        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .displayOrder(category.getDisplayOrder())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(children)
                .build();
    }
}
