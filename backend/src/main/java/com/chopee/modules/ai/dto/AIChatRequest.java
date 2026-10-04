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

    /**
     * User's personal AI API Key (BYOK - Bring Your Own Key, e.g. Gemini / OpenAI).
     * If provided, requests run under the user's personal quota rather than the server's shared quota.
     */
    private String apiKey;
}
