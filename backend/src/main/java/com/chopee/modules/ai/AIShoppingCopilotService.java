package com.chopee.modules.ai;

import com.chopee.entity.Product;
import com.chopee.entity.enums.ProductStatus;
import com.chopee.modules.ai.dto.AIChatRequest;
import com.chopee.modules.ai.dto.AIChatResponse;
import com.chopee.modules.catalog.ProductService;
import com.chopee.modules.catalog.dto.ProductSummaryResponse;
import com.chopee.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIShoppingCopilotService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Value("${chopee.ai.api-key:}")
    private String apiKey;

    @Value("${chopee.ai.provider:gemini}")
    private String provider;

    @Value("${chopee.ai.model:gemini-3.5-flash}")
    private String modelName;

    private final RestClient restClient = RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory() {{
                setConnectTimeout(5000);
                setReadTimeout(15000);
            }})
            .build();

    // Bộ từ điển ánh xạ món ăn sang nguyên liệu tươi sống tiêu biểu
    private static final Map<String, List<String>> RECIPE_INGREDIENTS_MAP = new LinkedHashMap<>();

    static {
        RECIPE_INGREDIENTS_MAP.put("canh chua", List.of("cá", "cà chua", "thơm", "dứa", "đậu bắp", "bạc hà", "giá", "ngò"));
        RECIPE_INGREDIENTS_MAP.put("lẩu thái", List.of("tôm", "mực", "nấm", "sả", "ớt", "cải", "rau muống", "bún"));
        RECIPE_INGREDIENTS_MAP.put("thịt kho", List.of("thịt", "ba chỉ", "trứng", "nước dừa", "hành"));
        RECIPE_INGREDIENTS_MAP.put("bò kho", List.of("bò", "thịt bò", "cà rốt", "sả", "hành"));
        RECIPE_INGREDIENTS_MAP.put("gà nướng", List.of("gà", "thịt gà", "mật ong", "tiêu", "ớt"));
        RECIPE_INGREDIENTS_MAP.put("cá kho", List.of("cá", "tiêu", "hành", "ớt", "nước mắm"));
        RECIPE_INGREDIENTS_MAP.put("súp", List.of("gà", "nấm", "ngô", "bắp", "trứng", "cà rốt"));
        RECIPE_INGREDIENTS_MAP.put("sinh tố", List.of("bơ", "xoài", "chuối", "dâu", "sữa"));
        RECIPE_INGREDIENTS_MAP.put("nước ép", List.of("cam", "táo", "dưa hấu", "ổi", "dứa"));
        RECIPE_INGREDIENTS_MAP.put("salad", List.of("xà lách", "cà chua", "dưa leo", "dầu giấm"));
        RECIPE_INGREDIENTS_MAP.put("rau xào", List.of("rau muống", "cải", "nấm", "tỏi"));
    }

    private static final List<String> TECH_APPLIANCE_KEYWORDS = List.of(
            "nồi chiên", "nồi cơm", "bếp từ", "máy xay", "ấm siêu tốc", "bàn là",
            "tai nghe", "chuột", "bàn phím", "sạc dự phòng", "cáp sạc", "quạt", "loa bluetooth"
    );

    @Transactional(readOnly = true)
    public AIChatResponse chat(AIChatRequest request) {
        String userMsg = request.getMessage() != null ? request.getMessage().trim() : "";
        String lowerMsg = userMsg.toLowerCase();

        // 1. Phân loại ý định (Intent Recognition)
        String intent = detectIntent(lowerMsg);

        // 2. Trích xuất ngân sách nếu có
        BigDecimal budget = request.getMaxBudget() != null ? request.getMaxBudget() : extractBudgetFromText(lowerMsg);

        // 3. Trích xuất từ khóa tìm kiếm sản phẩm theo ngữ cảnh (Context Enrichment)
        List<String> searchKeywords = extractKeywords(lowerMsg, intent);

        // 4. Truy vấn sản phẩm thực tế từ Database
        List<ProductSummaryResponse> recommendedProducts = searchProductsFromDatabase(searchKeywords, budget);

        // 5. Xác định API key hiệu lực (ưu tiên BYOK từ request/header, sau đó là server key)
        String effectiveApiKey = (request.getApiKey() != null && !request.getApiKey().isBlank())
                ? request.getApiKey().trim()
                : this.apiKey;

        // 6. Sinh nội dung tư vấn: Ưu tiên gọi Google Gemini API với RAG context nếu có API key
        String reply = null;
        if (effectiveApiKey != null && !effectiveApiKey.isBlank()) {
            reply = callGeminiAPI(effectiveApiKey, userMsg, intent, recommendedProducts, budget);
        }

        // Nếu Gemini không phản hồi hoặc không có API key -> dùng Fallback Engine nội bộ
        if (reply == null || reply.isBlank()) {
            reply = generateAdviceReply(userMsg, intent, recommendedProducts, budget);
        }

        // 7. Gợi ý các câu hỏi tiếp theo để khách bấm nhanh
        List<String> suggestedQuestions = generateSuggestedQuestions(intent, lowerMsg);

        return AIChatResponse.builder()
                .reply(reply)
                .intent(intent)
                .recommendedProducts(recommendedProducts)
                .suggestedQuestions(suggestedQuestions)
                .build();
    }

    private String callGeminiAPI(String apiKeyToUse, String userMsg, String intent, List<ProductSummaryResponse> products, BigDecimal budget) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKeyToUse;

            StringBuilder prompt = new StringBuilder();
            prompt.append("Bạn là Trợ lý Mua sắm AI thông minh của sàn thương mại điện tử Chopee (Việt Nam).\n");
            prompt.append("Hãy tư vấn cho người dùng với phong cách thân thiện, chu đáo, súc tích và nhiệt tình bằng tiếng Việt (sử dụng định dạng Markdown đẹp mắt).\n\n");

            if (products != null && !products.isEmpty()) {
                prompt.append("Các sản phẩm thực tế đang có sẵn trên sàn Chopee phù hợp với câu hỏi của khách:\n");
                for (ProductSummaryResponse p : products) {
                    prompt.append(String.format("- %s (Giá: %,d đ, Đơn vị: %s, Gian hàng: %s)\n",
                            p.getName(), p.getSellingPrice().longValue(), p.getUnit(), p.getShopName()));
                }
                prompt.append("\nHãy hướng dẫn khách cách mua sắm các sản phẩm trên, nhấn mạnh rằng họ có thể bấm nút 'Thêm vào giỏ hàng' trực tiếp tại các thẻ sản phẩm bên dưới màn hình chat.\n");
            }

            if (budget != null) {
                prompt.append(String.format("\nNgân sách của khách: %,d đ. Hãy lưu ý tối ưu chi tiêu trong ngân sách này.\n", budget.longValue()));
            }

            prompt.append("\nCâu hỏi của khách: ").append(userMsg);

            Map<String, Object> part = Map.of("text", prompt.toString());
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> generationConfig = Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 2048
            );
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", generationConfig
            );

            String responseJson = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseJson != null && !responseJson.isBlank()) {
                JsonNode root = objectMapper.readTree(responseJson);
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                    if (!textNode.isMissingNode() && !textNode.asText().isBlank()) {
                        return textNode.asText();
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Gọi Google Gemini API thất bại ({}), tự động kích hoạt bộ xử lý miền nội bộ: {}",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
        return null;
    }

    private String detectIntent(String lowerMsg) {
        // Kiểm tra ẩm thực / nấu ăn
        for (String dish : RECIPE_INGREDIENTS_MAP.keySet()) {
            if (lowerMsg.contains(dish)) {
                return "COOKING_RECIPE";
            }
        }
        if (lowerMsg.contains("nấu") || lowerMsg.contains("món") || lowerMsg.contains("công thức")
                || lowerMsg.contains("thực đơn") || lowerMsg.contains("đi chợ") || lowerMsg.contains("nguyên liệu")) {
            return "COOKING_RECIPE";
        }

        // Kiểm tra đồ gia dụng & công nghệ
        for (String tech : TECH_APPLIANCE_KEYWORDS) {
            if (lowerMsg.contains(tech)) {
                return "TECH_ADVICE";
            }
        }
        if (lowerMsg.contains("công suất") || lowerMsg.contains("bảo hành") || lowerMsg.contains("thông số")
                || lowerMsg.contains("gia dụng") || lowerMsg.contains("điện tử")) {
            return "TECH_ADVICE";
        }

        // Kiểm tra ngân sách / deal
        if (lowerMsg.contains("ngân sách") || lowerMsg.contains("dưới") || lowerMsg.contains("tầm giá")
                || lowerMsg.contains("combo") || lowerMsg.contains("săn deal") || lowerMsg.contains("giảm giá")) {
            return "BUDGET_SHOPPING";
        }

        return "GENERAL_ASSISTANT";
    }

    private BigDecimal extractBudgetFromText(String text) {
        Pattern pattern = Pattern.compile("(dưới|tầm|khoảng|ngân sách)?\\s*(\\d+[.,]?\\d*)\\s*(k|tr|triệu|nghìn|vnd|đ)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                String numStr = matcher.group(2).replace(",", ".");
                double num = Double.parseDouble(numStr);
                String unit = matcher.group(3) != null ? matcher.group(3).toLowerCase() : "";

                if (unit.contains("tr") || unit.contains("triệu")) {
                    return BigDecimal.valueOf(num * 1_000_000);
                } else if (unit.contains("k") || unit.contains("nghìn") || num < 1000) {
                    return BigDecimal.valueOf(num * 1_000);
                } else {
                    return BigDecimal.valueOf(num);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private List<String> extractKeywords(String lowerMsg, String intent) {
        Set<String> keywords = new LinkedHashSet<>();

        // Kiểm tra xem có trùng món ăn nào không
        for (Map.Entry<String, List<String>> entry : RECIPE_INGREDIENTS_MAP.entrySet()) {
            if (lowerMsg.contains(entry.getKey())) {
                keywords.addAll(entry.getValue());
                break;
            }
        }

        // Kiểm tra thiết bị gia dụng / công nghệ
        for (String tech : TECH_APPLIANCE_KEYWORDS) {
            if (lowerMsg.contains(tech)) {
                keywords.add(tech);
            }
        }

        // Nếu chưa tìm được từ khóa nào, lấy các từ có nghĩa từ câu hỏi của người dùng
        if (keywords.isEmpty()) {
            String clean = lowerMsg.replaceAll("[^a-zA-Z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ\\s]", " ");
            String[] tokens = clean.split("\\s+");
            Set<String> stopWords = Set.of("tôi", "muốn", "cần", "tìm", "mua", "bán", "giúp", "với", "có", "không", "ở", "đâu", "nào", "cho", "của", "và");
            for (String t : tokens) {
                if (t.length() >= 2 && !stopWords.contains(t)) {
                    keywords.add(t);
                }
            }
        }

        return new ArrayList<>(keywords);
    }

    private List<ProductSummaryResponse> searchProductsFromDatabase(List<String> keywords, BigDecimal budget) {
        Set<Long> seenProductIds = new HashSet<>();
        List<Product> matchedProducts = new ArrayList<>();

        for (String kw : keywords) {
            List<Product> products = productRepository.searchByKeyword(kw);
            for (Product p : products) {
                if (p.getStatus() == ProductStatus.ACTIVE
                        && p.getStockQuantity() != null
                        && p.getStockQuantity().compareTo(BigDecimal.ZERO) > 0
                        && !seenProductIds.contains(p.getId())) {

                    // Lọc theo ngân sách nếu có
                    if (budget != null && p.getSellingPrice().compareTo(budget) > 0) {
                        continue;
                    }

                    seenProductIds.add(p.getId());
                    matchedProducts.add(p);
                }
            }
            if (matchedProducts.size() >= 8) break;
        }

        // Nếu chưa đủ và không có budget lọc quá gắt, lấy thêm vài sản phẩm nổi bật
        if (matchedProducts.isEmpty()) {
            List<Product> generalList = productRepository.findAll();
            for (Product p : generalList) {
                if (p.getStatus() == ProductStatus.ACTIVE
                        && p.getStockQuantity() != null
                        && p.getStockQuantity().compareTo(BigDecimal.ZERO) > 0
                        && (budget == null || p.getSellingPrice().compareTo(budget) <= 0)) {
                    matchedProducts.add(p);
                    if (matchedProducts.size() >= 4) break;
                }
            }
        }

        return matchedProducts.stream()
                .limit(8)
                .map(productService::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    private String generateAdviceReply(String userMsg, String intent, List<ProductSummaryResponse> products, BigDecimal budget) {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        StringBuilder sb = new StringBuilder();

        switch (intent) {
            case "COOKING_RECIPE":
                sb.append("### 🍲 Bí Quyết Nấu Ngon & Đi Chợ Thông Minh\n\n");
                sb.append("Chào bạn! Dưới đây là gợi ý công thức và các nguyên liệu tươi ngon nhất từ các gian hàng uy tín trên **Chopee**:\n\n");
                sb.append("✨ **Hướng dẫn nấu:**\n");
                sb.append("1. **Sơ chế:** Rửa sạch rau củ quả bằng nước muối loãng; cá/thịt làm sạch và ướp nhẹ gia vị trong 10-15 phút để đậm đà.\n");
                sb.append("2. **Chế biến:** Phi thơm tỏi ớt, đun sôi nước dùng và cho nguyên liệu theo thứ tự độ chín để giữ trọn vị tươi ngọt.\n");
                sb.append("3. **Trình bày:** Nêm nếm vừa ăn, thêm rau thơm và dùng nóng để thưởng thức trọn vị ngon!\n\n");
                if (!products.isEmpty()) {
                    sb.append("🛒 **Nguyên liệu đang có sẵn giao ngay trên Chopee:**\n");
                    sb.append("Bạn có thể bấm **\"Thêm vào giỏ hàng\"** trực tiếp ở các thẻ sản phẩm bên dưới để chuẩn bị bữa cơm trọn vẹn nhé!");
                } else {
                    sb.append("Hiện tại các nguyên liệu chuyên biệt cho món này đang được cập nhật thêm hàng mới, bạn hãy tham khảo thêm các gian hàng nông sản trên sàn nhé!");
                }
                break;

            case "TECH_ADVICE":
                sb.append("### ⚡ Tư Vấn Đồ Gia Dụng & Thiết Bị Công Nghệ\n\n");
                sb.append("Chào bạn! Khi chọn mua thiết bị công nghệ & gia dụng, bạn nên lưu ý các tiêu chí sau:\n\n");
                sb.append("- **Công suất & Dung tích:** Chọn phù hợp với số lượng thành viên trong gia đình để tiết kiệm điện năng.\n");
                sb.append("- **Chất liệu an toàn:** Ưu tiên chống dính cao cấp, nhựa ABS kháng vỡ, inox 304 không gỉ.\n");
                sb.append("- **Bảo hành chính hãng:** Các sản phẩm trên Chopee đều được cam kết bảo hành chính hãng từ 12 - 24 tháng.\n\n");
                if (!products.isEmpty()) {
                    sb.append("📱 **Sản phẩm đề xuất tốt nhất dành cho bạn:**\n");
                    sb.append("Danh sách các model được đánh giá cao kèm giá ưu đãi bên dưới:");
                }
                break;

            case "BUDGET_SHOPPING":
                sb.append("### 💰 Gợi Ý Giỏ Hàng Tiết Kiệm Tối Ưu\n\n");
                if (budget != null) {
                    sb.append("Với mức ngân sách khoảng **").append(currencyFormat.format(budget)).append(" đ**, ");
                    sb.append("tôi đã chọn lọc các sản phẩm chất lượng cao có mức giá hợp lý nhất:\n\n");
                } else {
                    sb.append("Dưới đây là các combo sản phẩm có mức giá cực tốt cùng nhiều mã khuyến mãi đang áp dụng:\n\n");
                }
                sb.append("- Áp dụng thêm Voucher Freeship hoặc mã giảm giá của từng shop khi đặt đơn hàng để tiết kiệm thêm!\n\n");
                sb.append("🛍️ **Danh sách sản phẩm phù hợp ngân sách:**");
                break;

            default:
                sb.append("### 🤖 Trợ Lý Mua Sắm Toàn Năng Chopee\n\n");
                sb.append("Xin chào! Tôi là Trợ lý Mua sắm AI của Chopee. Tôi có thể hỗ trợ bạn:\n\n");
                sb.append("🥗 **Lên thực đơn & chuẩn bị nguyên liệu đi chợ tươi sống hàng ngày**\n");
                sb.append("🔌 **Tư vấn thông số kỹ thuật đồ gia dụng, thiết bị công nghệ**\n");
                sb.append("💡 **Gợi ý giỏ hàng thông minh theo ngân sách mong muốn**\n\n");
                if (!products.isEmpty()) {
                    sb.append("Dưới đây là một số sản phẩm nổi bật đang được ưa chuộng trên chợ hôm nay:");
                } else {
                    sb.append("Bạn đang quan tâm đến món ăn hay mặt hàng nào? Hãy chia sẻ cho tôi biết nhé!");
                }
                break;
        }

        return sb.toString();
    }

    private List<String> generateSuggestedQuestions(String intent, String userMsg) {
        switch (intent) {
            case "COOKING_RECIPE":
                return List.of(
                        "Có cần mua thêm gia vị gì cho món này không?",
                        "Gợi ý thêm món xào hoặc món mặn ăn kèm",
                        "Xem mẹo bảo quản rau củ tươi sống trong tủ lạnh"
                );
            case "TECH_ADVICE":
                return List.of(
                        "So sánh công suất và mức tiêu thụ điện",
                        "Chính sách bảo hành và đổi trả trên Chopee",
                        "Xem thêm các sản phẩm gia dụng đang có deal hời"
                );
            case "BUDGET_SHOPPING":
                return List.of(
                        "Có mã voucher freeship nào cho đơn này không?",
                        "Gợi ý combo dưới 200.000 đ",
                        "Cách gom hàng cùng một shop để giảm phí ship"
                );
            default:
                return List.of(
                        "Hôm nay chợ có rau củ gì tươi ngon?",
                        "Gợi ý thực đơn bữa cơm gia đình 4 người",
                        "Top đồ gia dụng nhà bếp bán chạy nhất"
                );
        }
    }
}
