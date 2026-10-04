package com.chopee.modules.ai.dto;

import com.chopee.modules.catalog.dto.ProductSummaryResponse;
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
public class AIChatResponse {

    private String reply;

    private String intent;

    @Builder.Default
    private List<ProductSummaryResponse> recommendedProducts = new ArrayList<>();

    @Builder.Default
    private List<String> suggestedQuestions = new ArrayList<>();
}
