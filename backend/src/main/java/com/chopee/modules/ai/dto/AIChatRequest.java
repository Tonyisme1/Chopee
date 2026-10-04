package com.chopee.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatRequest {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String message;

    private List<ChatMessageDTO> history;

    private BigDecimal maxBudget;
}
