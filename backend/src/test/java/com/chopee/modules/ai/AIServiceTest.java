package com.chopee.modules.ai;

import com.chopee.common.dto.ApiResponse;
import com.chopee.entity.Category;
import com.chopee.entity.Product;
import com.chopee.entity.Shop;
import com.chopee.entity.User;
import com.chopee.entity.enums.ProductStatus;
import com.chopee.entity.enums.Role;
import com.chopee.entity.enums.ShopStatus;
import com.chopee.entity.enums.ShopType;
import com.chopee.entity.enums.StorageType;
import com.chopee.entity.enums.UserStatus;
import com.chopee.modules.ai.dto.AIChatRequest;
import com.chopee.modules.ai.dto.AIChatResponse;
import com.chopee.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AIServiceTest {

    @Autowired
    private AIShoppingCopilotService aiService;

    @Autowired
    private AIController aiController;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    private Shop shop;
    private Category categoryFood;
    private Category categoryTech;

    @BeforeEach
    void setUp() {
        orderStatusHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        shopRepository.deleteAll();
        userRepository.deleteAll();

        User seller = userRepository.save(User.builder()
                .username("seller_ai_test")
                .passwordHash("hashed")
                .email("seller_ai@chopee.vn")
                .fullName("Chủ Tiệm Bách Hóa")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shop = shopRepository.save(Shop.builder()
                .user(seller)
                .name("Siêu Thị Tiện Lợi")
                .slug("sieu-thi-tien-loi")
                .shopType(ShopType.GENERAL)
                .status(ShopStatus.APPROVED)
                .build());

        categoryFood = categoryRepository.save(Category.builder()
                .name("Thực phẩm tươi sống")
                .slug("thuc-pham-tuoi-song")
                .build());

        categoryTech = categoryRepository.save(Category.builder()
                .name("Đồ gia dụng & Công nghệ")
                .slug("do-gia-dung-cong-nghe")
                .build());

        // 1. Thực phẩm nấu canh chua
        productRepository.save(Product.builder()
                .shop(shop)
                .category(categoryFood)
                .name("Cá basa phi lê tươi ngon")
                .slug("ca-basa-phi-le-tuoi-ngon")
                .thumbnailUrl("https://img.chopee.vn/fish.jpg")
                .originalPrice(new BigDecimal("60000"))
                .sellingPrice(new BigDecimal("50000"))
                .stockQuantity(new BigDecimal("20.0"))
                .unit("kg")
                .storageType(StorageType.FRESH)
                .status(ProductStatus.ACTIVE)
                .build());

        productRepository.save(Product.builder()
                .shop(shop)
                .category(categoryFood)
                .name("Cà chua sạch VietGAP")
                .slug("ca-chua-sach-vietgap")
                .thumbnailUrl("https://img.chopee.vn/tomato.jpg")
                .originalPrice(new BigDecimal("30000"))
                .sellingPrice(new BigDecimal("25000"))
                .stockQuantity(new BigDecimal("50.0"))
                .unit("kg")
                .storageType(StorageType.FRESH)
                .status(ProductStatus.ACTIVE)
                .build());

        productRepository.save(Product.builder()
                .shop(shop)
                .category(categoryFood)
                .name("Đậu bắp xanh giòn")
                .slug("dau-bap-xanh-gion")
                .thumbnailUrl("https://img.chopee.vn/okra.jpg")
                .originalPrice(new BigDecimal("20000"))
                .sellingPrice(new BigDecimal("15000"))
                .stockQuantity(new BigDecimal("30.0"))
                .unit("kg")
                .storageType(StorageType.FRESH)
                .status(ProductStatus.ACTIVE)
                .build());

        // 2. Thiết bị công nghệ / gia dụng
        productRepository.save(Product.builder()
                .shop(shop)
                .category(categoryTech)
                .name("Nồi chiên không dầu Philips 5.5L")
                .slug("noi-chien-khong-dau-philips-55l")
                .thumbnailUrl("https://img.chopee.vn/airfryer.jpg")
                .originalPrice(new BigDecimal("2200000"))
                .sellingPrice(new BigDecimal("1850000"))
                .stockQuantity(new BigDecimal("15.0"))
                .unit("chiếc")
                .status(ProductStatus.ACTIVE)
                .build());

        // 3. Món đắt tiền phục vụ test lọc ngân sách
        productRepository.save(Product.builder()
                .shop(shop)
                .category(categoryFood)
                .name("Thịt bò Wagyu A5 thượng hạng")
                .slug("thit-bo-wagyu-a5-thuong-hang")
                .thumbnailUrl("https://img.chopee.vn/wagyu.jpg")
                .originalPrice(new BigDecimal("1500000"))
                .sellingPrice(new BigDecimal("1200000"))
                .stockQuantity(new BigDecimal("5.0"))
                .unit("kg")
                .storageType(StorageType.FROZEN_CHILLED)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Hỏi đáp nấu ăn (Canh chua): Nhận diện đúng ý định COOKING_RECIPE và gắn kèm các nguyên liệu cá, cà chua, đậu bắp có sẵn")
    void testCookingIntent_CanhChua_ReturnsIngredientsAndProducts() {
        AIChatRequest request = AIChatRequest.builder()
                .message("Trưa nay muốn nấu canh chua cá thì cần chuẩn bị những nguyên liệu gì?")
                .build();

        AIChatResponse response = aiService.chat(request);

        assertNotNull(response);
        assertEquals("COOKING_RECIPE", response.getIntent());
        assertNotNull(response.getReply());
        assertTrue(response.getReply().contains("Canh") || response.getReply().contains("Nấu Ngon") || response.getReply().contains("Chopee"));
        assertFalse(response.getRecommendedProducts().isEmpty(), "Phải trả về danh sách sản phẩm nguyên liệu từ DB");

        // Kiểm tra xem có sản phẩm cá hoặc cà chua hoặc đậu bắp trong danh sách gợi ý
        boolean hasIngredient = response.getRecommendedProducts().stream()
                .anyMatch(p -> p.getName().toLowerCase().contains("cá")
                        || p.getName().toLowerCase().contains("cà chua")
                        || p.getName().toLowerCase().contains("đậu bắp"));
        assertTrue(hasIngredient, "Sản phẩm gợi ý phải chứa nguyên liệu nấu canh chua");

        // Kiểm tra gợi ý câu hỏi tiếp theo
        assertNotNull(response.getSuggestedQuestions());
        assertEquals(3, response.getSuggestedQuestions().size());
    }

    @Test
    @DisplayName("Tư vấn công nghệ (Nồi chiên không dầu): Nhận diện đúng ý định TECH_ADVICE và trả về model phù hợp")
    void testTechAdviceIntent_AirFryer() {
        AIChatRequest request = AIChatRequest.builder()
                .message("Tư vấn giúp mình mua một chiếc nồi chiên không dầu tốt cho gia đình")
                .build();

        AIChatResponse response = aiService.chat(request);

        assertNotNull(response);
        assertEquals("TECH_ADVICE", response.getIntent());
        assertTrue(response.getReply().contains("Gia Dụng") || response.getReply().contains("Công Nghệ"));

        boolean hasAirFryer = response.getRecommendedProducts().stream()
                .anyMatch(p -> p.getName().toLowerCase().contains("nồi chiên không dầu"));
        assertTrue(hasAirFryer, "Phải gợi ý Nồi chiên không dầu Philips có trong sàn");
    }

    @Test
    @DisplayName("Lọc theo ràng buộc ngân sách: Chỉ trả về sản phẩm có giá <= ngân sách yêu cầu (loại trừ thịt bò Wagyu)")
    void testBudgetConstraintFiltering() {
        // Ngân sách 30.000 đ
        AIChatRequest request = AIChatRequest.builder()
                .message("Tìm đồ ăn trưa ngân sách dưới 30k")
                .maxBudget(new BigDecimal("30000"))
                .build();

        AIChatResponse response = aiService.chat(request);

        assertNotNull(response);
        assertFalse(response.getRecommendedProducts().isEmpty());

        for (var p : response.getRecommendedProducts()) {
            assertTrue(p.getSellingPrice().compareTo(new BigDecimal("30000")) <= 0,
                    "Giá sản phẩm " + p.getName() + " (" + p.getSellingPrice() + ") phải <= 30.000");
        }

        boolean containsExpensiveWagyu = response.getRecommendedProducts().stream()
                .anyMatch(p -> p.getName().toLowerCase().contains("wagyu"));
        assertFalse(containsExpensiveWagyu, "Không được gợi ý thịt bò Wagyu 1.2 triệu khi ngân sách chỉ 30k");
    }

    @Test
    @DisplayName("Chào hỏi chung (General Assistant): Trả về hướng dẫn sử dụng và câu hỏi gợi ý khám phá sàn")
    void testGeneralAssistantFallback() {
        AIChatRequest request = AIChatRequest.builder()
                .message("Xin chào Chopee bot, hôm nay có gì đặc biệt không?")
                .build();

        AIChatResponse response = aiService.chat(request);

        assertNotNull(response);
        assertEquals("GENERAL_ASSISTANT", response.getIntent());
        assertTrue(response.getReply().contains("Trợ Lý Mua Sắm"));
        assertEquals(3, response.getSuggestedQuestions().size());
    }

    @Test
    @DisplayName("REST Controller: Endpoint POST /api/v1/ai/chat phản hồi HTTP 200 OK với cấu trúc chuẩn ApiResponse")
    void testAIControllerEndpoint() {
        AIChatRequest request = AIChatRequest.builder()
                .message("Cho mình xem một số rau củ tươi sạch hôm nay")
                .build();

        ResponseEntity<ApiResponse<AIChatResponse>> responseEntity = aiController.chat(request);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().isSuccess());
        assertNotNull(responseEntity.getBody().getData());
        assertNotNull(responseEntity.getBody().getData().getReply());
    }
}
