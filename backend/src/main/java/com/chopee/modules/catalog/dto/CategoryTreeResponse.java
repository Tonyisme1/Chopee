package com.chopee.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeResponse {
    private Long id;
    private String name;
    private String slug;
    private String iconUrl;
    private Integer displayOrder;
    private Long parentId;
    @Builder.Default
    private List<CategoryTreeResponse> children = new ArrayList<>();
}
