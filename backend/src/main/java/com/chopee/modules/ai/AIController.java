package com.chopee.modules.ai;

import com.chopee.common.dto.ApiResponse;
import com.chopee.modules.ai.dto.AIChatRequest;
import com.chopee.modules.ai.dto.AIChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "AI Shopping Copilot", description = "APIs trợ lý mua sắm AI thông minh Chopee")
public class AIController {

    private final AIShoppingCopilotService aiShoppingCopilotService;

    @PostMapping("/chat")
    @Operation(summary = "Hỏi đáp thực đơn, tư vấn đồ gia dụng, săn deal và nhận thẻ sản phẩm từ AI Copilot (hỗ trợ BYOK header X-AI-API-Key)")
    public ResponseEntity<ApiResponse<AIChatResponse>> chat(
            @RequestHeader(value = "X-AI-API-Key", required = false) String headerApiKey,
            @Valid @RequestBody AIChatRequest request) {
        if ((request.getApiKey() == null || request.getApiKey().isBlank()) && headerApiKey != null && !headerApiKey.isBlank()) {
            request.setApiKey(headerApiKey.trim());
        }
        AIChatResponse response = aiShoppingCopilotService.chat(request);
        return ResponseEntity.ok(ApiResponse.success("Phản hồi thành công từ Trợ lý AI", response));
    }

    public ResponseEntity<ApiResponse<AIChatResponse>> chat(AIChatRequest request) {
        return chat(null, request);
    }
}
