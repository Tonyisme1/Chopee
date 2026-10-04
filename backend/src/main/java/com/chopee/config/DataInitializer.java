package com.chopee.config;

import com.chopee.entity.*;
import com.chopee.entity.enums.*;
import com.chopee.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            boolean hasGarbled = productRepository.findAll().stream()
                    .anyMatch(p -> p.getName() != null && (p.getName().contains("Ã") || p.getName().contains("á»")));
            if (hasGarbled) {
                log.info("Phát hiện dữ liệu mã hóa ký tự cũ, tự động xóa và nạp lại chuẩn UTF-8...");
                productRepository.deleteAll();
                categoryRepository.deleteAll();
                shopRepository.deleteAll();
            }
        }

        if (productRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu mẫu sàn Chopee Marketplace...");
            seedData();
            log.info("Khởi tạo dữ liệu mẫu thành công! Tổng sản phẩm: {}", productRepository.count());
        } else {
            log.info("Cơ sở dữ liệu đã có dữ liệu sản phẩm ({} sản phẩm), bỏ qua bước khởi tạo mẫu.", productRepository.count());
        }
    }

    private User getOrCreateUser(String username, String email, String fullName, String phone, String avatarUrl, Role role, String defaultPasswordHash) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(username)
                        .passwordHash(defaultPasswordHash)
                        .email(email)
                        .fullName(fullName)
                        .phone(phone)
                        .avatarUrl(avatarUrl)
                        .role(role)
                        .status(UserStatus.ACTIVE)
                        .build()));
    }

    private Shop getOrCreateShop(User user, String name, String slug, String description, ShopType shopType, String address, String phone, String logoUrl, String bannerUrl) {
        return shopRepository.findBySlug(slug)
                .orElseGet(() -> shopRepository.save(Shop.builder()
                        .user(user)
                        .name(name)
                        .slug(slug)
                        .description(description)
                        .shopType(shopType)
                        .status(ShopStatus.APPROVED)
                        .address(address)
                        .phone(phone)
                        .logoUrl(logoUrl)
                        .bannerUrl(bannerUrl)
                        .rating(new BigDecimal("4.9"))
                        .build()));
    }

    private Category getOrCreateCategory(String name, String slug, String iconUrl, Category parent, int displayOrder) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .slug(slug)
                        .iconUrl(iconUrl)
                        .parent(parent)
                        .displayOrder(displayOrder)
                        .build()));
    }

    @Transactional
    public void seedData() {
        String defaultPasswordHash = passwordEncoder.encode("123456");

        // 1. Tạo Tài khoản Người dùng
        User admin = getOrCreateUser("admin", "admin@chopee.vn", "Quản Trị Viên Chopee", "0901234567",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150", Role.ROLE_ADMIN, defaultPasswordHash);

        User sellerFood = getOrCreateUser("seller_food", "dalat_farm@chopee.vn", "Nguyễn Văn Nông", "0912345678",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150", Role.ROLE_SELLER, defaultPasswordHash);

        User sellerDrink = getOrCreateUser("seller_drink", "hungphat_beverage@chopee.vn", "Trần Hùng Phát", "0923456789",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150", Role.ROLE_SELLER, defaultPasswordHash);

        User sellerAppliance = getOrCreateUser("seller_appliances", "philips_mall@chopee.vn", "Lê Hoàng Philips", "0934567890",
                "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150", Role.ROLE_SELLER, defaultPasswordHash);

        User sellerTech = getOrCreateUser("seller_tech", "techzone_official@chopee.vn", "Vũ Minh Tech", "0945678901",
                "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150", Role.ROLE_SELLER, defaultPasswordHash);

        User sellerFashion = getOrCreateUser("seller_fashion", "unistyle_fashion@chopee.vn", "Đỗ Thảo Vy", "0956789012",
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150", Role.ROLE_SELLER, defaultPasswordHash);

        User buyer = getOrCreateUser("buyer1", "buyer1@chopee.vn", "Hoàng Thị Mua Sắm", "0988776655",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150", Role.ROLE_BUYER, defaultPasswordHash);

        // 2. Tạo 5 Gian hàng (Shops)
        Shop shopFood = getOrCreateShop(sellerFood, "Nông Sản Sạch Đà Lạt", "nong-san-sach-da-lat",
                "Chuyên cung cấp rau củ quả thủy canh VietGAP tươi ngon, thu hoạch hàng ngày tại nông trường Đà Lạt.",
                ShopType.FOOD_FRESH, "Phường 7, TP. Đà Lạt, Lâm Đồng", "0912345678",
                "https://images.unsplash.com/photo-1542838132-92c53300491e?w=200",
                "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=1200");

        Shop shopDrink = getOrCreateShop(sellerDrink, "Đại Lý Đồ Uống Hùng Phát", "dai-ly-do-uong-hung-phat",
                "Phân phối sỉ lẻ bia, nước ngọt, trà đóng chai chính hãng. Cam kết date mới, giao nhanh trong ngày.",
                ShopType.GENERAL, "Nguyễn Thị Minh Khai, Quận 1, TP. Hồ Chí Minh", "0923456789",
                "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=200",
                "https://images.unsplash.com/photo-1527061011665-3652c757a4d4?w=1200");

        Shop shopAppliance = getOrCreateShop(sellerAppliance, "Thế Giới Gia Dụng Philips", "the-gioi-gia-dung-philips",
                "Gian hàng chính hãng phân phối thiết bị gia dụng nhà bếp cao cấp Philips, Tefal, Lock&Lock.",
                ShopType.OFFICIAL_MALL, "Trần Duy Hưng, Cầu Giấy, Hà Nội", "0934567890",
                "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=200",
                "https://images.unsplash.com/photo-1556909212-d5b604d0c90d?w=1200");

        Shop shopTech = getOrCreateShop(sellerTech, "TechZone Official Store", "techzone-official-store",
                "Cửa hàng công nghệ hàng đầu: tai nghe chống ồn, bàn phím cơ, chuột không dây và phụ kiện sạc cao cấp.",
                ShopType.OFFICIAL_MALL, "Nguyễn Trãi, Thanh Xuân, Hà Nội", "0945678901",
                "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=200",
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200");

        Shop shopFashion = getOrCreateShop(sellerFashion, "UniStyle - Thời Trang & Phụ Kiện", "unistyle-thoi-trang",
                "Thương hiệu thời trang basic tối giản, áo thun cotton thoáng mát, áo khoác cản gió và balo tiện ích.",
                ShopType.GENERAL, "Chùa Bộc, Đống Đa, Hà Nội", "0956789012",
                "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=200",
                "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1200");

        // 3. Tạo Cây Danh mục Sản phẩm (Categories)
        Category catFoodRoot = getOrCreateCategory("Thực phẩm tươi sống", "thuc-pham-tuoi-song", "🥬", null, 1);
        Category catVeggies = getOrCreateCategory("Rau củ quả tươi", "rau-cu-qua-tuoi", "🥕", catFoodRoot, 1);
        Category catMeatFish = getOrCreateCategory("Thịt & Thủy hải sản", "thit-thuy-hai-san", "🐟", catFoodRoot, 2);

        Category catDrinkRoot = getOrCreateCategory("Đồ uống & Giải khát", "do-uong-giai-khat", "🥤", null, 2);
        Category catBeer = getOrCreateCategory("Bia & Đồ uống có cồn", "bia-do-uong-co-con", "🍺", catDrinkRoot, 1);
        Category catSoftDrink = getOrCreateCategory("Nước ngọt & Trà giải nhiệt", "nuoc-ngot-tra-giai-nhiet", "🧃", catDrinkRoot, 2);

        Category catApplianceRoot = getOrCreateCategory("Thiết bị gia dụng", "thiet-bi-gia-dung", "🍳", null, 3);
        Category catKitchen = getOrCreateCategory("Nồi chiên & Bếp điện", "noi-chien-bep-dien", "🥘", catApplianceRoot, 1);
        Category catBlender = getOrCreateCategory("Máy xay & Máy ép", "may-xay-may-ep", "🍹", catApplianceRoot, 2);

        Category catTechRoot = getOrCreateCategory("Phụ kiện công nghệ", "phu-kien-cong-nghe", "🎧", null, 4);
        Category catAudio = getOrCreateCategory("Tai nghe & Loa", "tai-nghe-loa", "🔊", catTechRoot, 1);
        Category catPeripherals = getOrCreateCategory("Bàn phím & Chuột", "ban-phim-chuot", "⌨️", catTechRoot, 2);
        Category catPower = getOrCreateCategory("Pin sạc & Cáp dữ liệu", "pin-sac-cap-du-lieu", "🔋", catTechRoot, 3);

        Category catFashionRoot = getOrCreateCategory("Thời trang & Phong cách", "thoi-trang-phong-cach", "👕", null, 5);

        // 4. Tạo Sản phẩm Thực phẩm Tươi sống (10 Sản phẩm)
        createProduct(shopFood, catVeggies,
                "Cà chua beef Đà Lạt mọng nước", "ca-chua-beef-da-lat",
                "Cà chua beef quả to dày cùi, mọng nước, chuẩn canh tác VietGAP tại Lâm Đồng. Thích hợp nấu canh chua, sốt và ăn sống.",
                "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500",
                new BigDecimal("35000"), new BigDecimal("28000"), new BigDecimal("120.0"),
                "kg", new BigDecimal("0.5"), new BigDecimal("0.5"), StorageType.FRESH, "5 ngày", "Đà Lạt, Lâm Đồng",
                "{\"cert\":\"VietGAP\",\"cultivation\":\"Thủy canh\"}", new BigDecimal("4.9"), 128);

        createProduct(shopFood, catVeggies,
                "Dưa leo baby giòn ngọt VietGAP", "dua-leo-baby-gion-ngot",
                "Dưa chuột baby vỏ mỏng giòn rụm, vị ngọt thanh tự nhiên, an toàn tuyệt đối không chất kích thích.",
                "https://images.unsplash.com/photo-1449300079323-02e209d9d3a6?w=500",
                new BigDecimal("30000"), new BigDecimal("24000"), new BigDecimal("80.0"),
                "kg", new BigDecimal("0.5"), new BigDecimal("0.5"), StorageType.FRESH, "7 ngày", "Đà Lạt, Lâm Đồng",
                "{\"cert\":\"VietGAP\",\"taste\":\"Giòn ngọt\"}", new BigDecimal("4.8"), 95);

        createProduct(shopFood, catVeggies,
                "Đậu bắp xanh tươi giòn Đà Lạt", "dau-bap-xanh-tuoi-gion",
                "Đậu bắp tươi non, màu xanh mướt, nhiều chất nhầy dinh dưỡng, nguyên liệu cốt lõi cho món canh chua cá và đồ nướng.",
                "https://images.unsplash.com/photo-1627916607164-7b20241db935?w=500",
                new BigDecimal("25000"), new BigDecimal("20000"), new BigDecimal("60.0"),
                "kg", new BigDecimal("0.5"), new BigDecimal("0.5"), StorageType.FRESH, "4 ngày", "Đà Lạt, Lâm Đồng",
                "{\"usage\":\"Nấu canh chua, nướng, luộc\"}", new BigDecimal("4.85"), 64);

        createProduct(shopFood, catVeggies,
                "Thơm mật (Dứa) chín cây vị ngọt lịm", "thom-mat-chin-cay",
                "Dứa mật ngọt đậm, hương thơm lừng, đã gọt sạch mắt, hoàn hảo để nấu canh chua hải sản hoặc ép nước thanh nhiệt.",
                "https://images.unsplash.com/photo-1550258987-190a2d41a8ba?w=500",
                new BigDecimal("22000"), new BigDecimal("18000"), new BigDecimal("90.0"),
                "trái", BigDecimal.ONE, BigDecimal.ONE, StorageType.FRESH, "6 ngày", "Tiền Giang",
                "{\"weight\":\"800g - 1kg/trái\"}", new BigDecimal("4.9"), 110);

        createProduct(shopFood, catVeggies,
                "Bắp cải trái tim ngọt mát Đà Lạt", "bap-cai-trai-tim-da-lat",
                "Bắp cải trái tim lá mềm, cuộn chặt, vị ngọt thanh tự nhiên thích hợp xào tỏi hoặc nấu súp thanh đạm.",
                "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=500",
                new BigDecimal("28000"), new BigDecimal("22000"), new BigDecimal("75.0"),
                "bắp", BigDecimal.ONE, BigDecimal.ONE, StorageType.FRESH, "10 ngày", "Đà Lạt, Lâm Đồng",
                "{\"cert\":\"VietGAP\"}", new BigDecimal("4.75"), 48);

        createProduct(shopFood, catMeatFish,
                "Cá basa phi lê không xương tươi ngon", "ca-basa-phi-le-khong-xuong",
                "Cá basa phi lê trắng muốt, không xương, đóng gói hút chân không, chuẩn vị béo ngọt cho món canh chua hoặc kho tộ.",
                "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=500",
                new BigDecimal("70000"), new BigDecimal("58000"), new BigDecimal("45.0"),
                "kg", new BigDecimal("0.5"), new BigDecimal("0.5"), StorageType.FRESH, "3 ngày (ngăn mát)", "Đồng Tháp",
                "{\"processing\":\"Phi lê lọc sạch xương\"}", new BigDecimal("4.9"), 156);

        createProduct(shopFood, catMeatFish,
                "Thịt ba chỉ heo sạch chuẩn CP", "thit-ba-chi-heo-sach-cp",
                "Thịt ba chỉ heo tươi dẻo, tỷ lệ nạc mỡ cân đối, không tồn dư kháng sinh, thích hợp làm món thịt kho tàu, luộc chấm mắm tôm.",
                "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=500",
                new BigDecimal("160000"), new BigDecimal("135000"), new BigDecimal("50.0"),
                "kg", new BigDecimal("0.5"), new BigDecimal("0.5"), StorageType.FRESH, "3 ngày", "Đồng Nai",
                "{\"standard\":\"Chuẩn thịt mát an toàn 3F\"}", new BigDecimal("4.85"), 210);

        createProduct(shopFood, catMeatFish,
                "Tôm sú tươi sống Cà Mau size lớn", "tom-su-tuoi-song-ca-mau",
                "Tôm sú thiên nhiên Cà Mau vỏ mỏng, thịt săn chắc ngọt đậm, nguyên liệu thượng hạng cho nồi lẩu thái hay hấp nước dừa.",
                "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=500",
                new BigDecimal("280000"), new BigDecimal("240000"), new BigDecimal("30.0"),
                "kg", new BigDecimal("0.5"), new BigDecimal("0.5"), StorageType.FRESH, "2 ngày", "Cà Mau",
                "{\"size\":\"20 - 25 con/kg\"}", new BigDecimal("4.95"), 89);

        createProduct(shopFood, catVeggies,
                "Nấm đùi gà hữu cơ tươi giòn", "nam-dui-ga-huu-co",
                "Nấm đùi gà thân mập mạp, vị giòn ngọt, giàu dinh dưỡng, thích hợp cho các món lẩu, xào chay và kho tiêu.",
                "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=500",
                new BigDecimal("35000"), new BigDecimal("28000"), new BigDecimal("65.0"),
                "khay", BigDecimal.ONE, BigDecimal.ONE, StorageType.FRESH, "7 ngày", "Hải Dương",
                "{\"weight\":\"300g/khay\"}", new BigDecimal("4.8"), 72);

        createProduct(shopFood, catVeggies,
                "Xà lách Lolo xanh thủy canh Đà Lạt", "xa-lach-lolo-xanh-thuy-canh",
                "Rau xà lách lolo giòn xốp, tươi non, rửa sạch dùng ngay cho các món salad dầu giấm và cuốn bánh tráng thịt luộc.",
                "https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=500",
                new BigDecimal("25000"), new BigDecimal("19000"), new BigDecimal("85.0"),
                "túi", BigDecimal.ONE, BigDecimal.ONE, StorageType.FRESH, "5 ngày", "Đà Lạt, Lâm Đồng",
                "{\"weight\":\"500g/túi\"}", new BigDecimal("4.85"), 93);

        // 5. Tạo Sản phẩm Đồ uống & Giải khát (6 Sản phẩm)
        createProduct(shopDrink, catBeer,
                "Thùng 24 lon Bia Heineken Silver 330ml", "thung-24-lon-bia-heineken-silver",
                "Bia Heineken Silver êm đượm nhẹ êm, thiết kế thời thượng, hương vị nhẹ nhàng sảng khoái cho mọi bữa tiệc sum họp.",
                "https://images.unsplash.com/photo-1608270586620-248524c67de9?w=500",
                new BigDecimal("450000"), new BigDecimal("415000"), new BigDecimal("150.0"),
                "thùng", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, "12 tháng", "Việt Nam",
                "{\"abv\":\"4.0%\",\"volume\":\"330ml/lon\",\"packaging\":\"Thùng 24 lon\"}", new BigDecimal("4.9"), 340);

        createProduct(shopDrink, catBeer,
                "Thùng 24 lon Bia Tiger Crystal 330ml", "thung-24-lon-bia-tiger-crystal",
                "Bia Tiger Crystal được tinh lọc với kỹ thuật làm lạnh sâu độc đáo ở nhiệt độ -1°C, giữ trọn vị bia thuần khiết mát lạnh.",
                "https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=500",
                new BigDecimal("410000"), new BigDecimal("385000"), new BigDecimal("120.0"),
                "thùng", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, "12 tháng", "Việt Nam",
                "{\"abv\":\"4.6%\",\"volume\":\"330ml/lon\",\"packaging\":\"Thùng 24 lon\"}", new BigDecimal("4.85"), 280);

        createProduct(shopDrink, catSoftDrink,
                "Thùng 24 lon Nước ngọt Coca-Cola vị nguyên bản 320ml", "thung-24-lon-coca-cola-320ml",
                "Coca-Cola chính hãng vị ga bùng nổ mát lạnh, xua tan cơn khát và tăng thêm vị ngon cho mọi bữa tiệc nướng lẩu.",
                "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=500",
                new BigDecimal("220000"), new BigDecimal("195000"), new BigDecimal("200.0"),
                "thùng", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, "12 tháng", "Việt Nam",
                "{\"volume\":\"320ml/lon\",\"packaging\":\"Thùng 24 lon\"}", new BigDecimal("4.9"), 510);

        createProduct(shopDrink, catSoftDrink,
                "Lốc 6 lon Nước ngọt Sprite Chanh tươi mát 320ml", "loc-6-lon-sprite-chanh-320ml",
                "Nước giải khát có ga vị chanh tươi mát rượi, giúp đập tan cơn khát tức thì và kích thích vị giác ăn ngon miệng hơn.",
                "https://images.unsplash.com/photo-1625772299848-391b6a87d7b3?w=500",
                new BigDecimal("60000"), new BigDecimal("52000"), new BigDecimal("180.0"),
                "lốc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, "12 tháng", "Việt Nam",
                "{\"volume\":\"320ml/lon\",\"packaging\":\"Lốc 6 lon\"}", new BigDecimal("4.8"), 145);

        createProduct(shopDrink, catSoftDrink,
                "Thùng 24 chai Nước khoáng thiên nhiên LaVie 500ml", "thung-24-chai-lavie-500ml",
                "Nước khoáng LaVie đóng chai nguồn khoáng ngầm tự nhiên, bổ sung các vi khoáng thiết yếu cho cơ thể khỏe khoắn mỗi ngày.",
                "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?w=500",
                new BigDecimal("115000"), new BigDecimal("98000"), new BigDecimal("250.0"),
                "thùng", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, "24 tháng", "Việt Nam",
                "{\"volume\":\"500ml/chai\",\"packaging\":\"Thùng 24 chai\"}", new BigDecimal("4.9"), 420);

        createProduct(shopDrink, catSoftDrink,
                "Lốc 6 chai Trà xanh Không Độ 455ml thanh nhiệt", "loc-6-chai-tra-xanh-khong-do",
                "Trà xanh Không Độ chiết xuất từ lá trà xanh Thái Nguyên nguyên chất, chứa hoạt chất EGCG giúp thanh lọc cơ thể không lo nóng.",
                "https://images.unsplash.com/photo-1556881286-fc6915169721?w=500",
                new BigDecimal("55000"), new BigDecimal("48000"), new BigDecimal("160.0"),
                "lốc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, "12 tháng", "Việt Nam",
                "{\"volume\":\"455ml/chai\",\"packaging\":\"Lốc 6 chai\"}", new BigDecimal("4.75"), 118);

        // 6. Tạo Sản phẩm Thiết bị Gia dụng (6 Sản phẩm)
        createProduct(shopAppliance, catKitchen,
                "Nồi chiên không dầu Philips HD9252/90 4.1L điện tử", "noi-chien-khong-dau-philips-hd9252",
                "Nồi chiên không dầu Philips công nghệ Rapid Air giảm tới 90% lượng mỡ thừa, màn hình cảm ứng 7 chế độ cài đặt sẵn, bảo hành 2 năm.",
                "https://images.unsplash.com/photo-1585515320310-259814833e62?w=500",
                new BigDecimal("2390000"), new BigDecimal("1850000"), new BigDecimal("35.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Trung Quốc (Chính hãng Philips)",
                "{\"power\":\"1400W\",\"capacity\":\"4.1L\",\"warranty\":\"24 tháng chính hãng\",\"tech\":\"Rapid Air\"}", new BigDecimal("4.95"), 180);

        createProduct(shopAppliance, catKitchen,
                "Nồi cơm điện cao tần Tefal Express Fuzzy 1.5L", "noi-com-dien-cao-tan-tefal-1-5l",
                "Nồi cơm điện cao tần lòng nồi niêu hợp kim nhôm 6 lớp chống dính bền bỉ, hạt cơm chín đều dẻo thơm từng hạt.",
                "https://images.unsplash.com/photo-1544233726-9f1d2b27be8b?w=500",
                new BigDecimal("1890000"), new BigDecimal("1490000"), new BigDecimal("25.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Pháp (Lắp ráp Trung Quốc)",
                "{\"power\":\"1200W\",\"capacity\":\"1.5L\",\"warranty\":\"24 tháng\"}", new BigDecimal("4.85"), 96);

        createProduct(shopAppliance, catBlender,
                "Máy xay sinh tố đa năng Philips ProBlend Crush HR2223", "may-xay-sinh-to-philips-hr2223",
                "Động cơ 700W mạnh mẽ cùng công nghệ ProBlend Crush 4 cánh xay đá tuyết mịn màng, kèm 3 cối thủy tinh kháng vỡ tiện dụng.",
                "https://images.unsplash.com/photo-1570222094114-d054a817e56b?w=500",
                new BigDecimal("1450000"), new BigDecimal("1150000"), new BigDecimal("40.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Chính hãng Philips",
                "{\"power\":\"700W\",\"accessories\":\"3 cối xay\",\"warranty\":\"24 tháng\"}", new BigDecimal("4.9"), 142);

        createProduct(shopAppliance, catKitchen,
                "Bếp điện từ đôi Inverter Sunhouse SHB9101 cảm ứng", "bep-tu-doi-inverter-sunhouse",
                "Bếp từ đôi Sunhouse mặt kính Ceramic chịu nhiệt cao cấp, công nghệ Inverter tiết kiệm điện tối ưu, mâm từ đồng nguyên chất 100%.",
                "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=500",
                new BigDecimal("4500000"), new BigDecimal("3690000"), new BigDecimal("15.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Việt Nam",
                "{\"power\":\"4400W\",\"inverter\":\"Có\",\"warranty\":\"36 tháng\"}", new BigDecimal("4.8"), 64);

        createProduct(shopAppliance, catKitchen,
                "Ấm siêu tốc thủy tinh Lock&Lock EJK418SLV 1.8L đèn LED", "am-sieu-toc-thuy-tinh-locknlock",
                "Thân ấm bằng thủy tinh Borosilicate chịu sốc nhiệt cao cấp, đèn LED xanh dương hiện đại khi đun sôi nước trong 3 phút.",
                "https://images.unsplash.com/photo-1594213114663-dd9571ff7669?w=500",
                new BigDecimal("690000"), new BigDecimal("499000"), new BigDecimal("60.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Hàn Quốc",
                "{\"power\":\"1850W\",\"capacity\":\"1.8L\",\"material\":\"Thủy tinh chịu nhiệt\"}", new BigDecimal("4.85"), 215);

        createProduct(shopAppliance, catKitchen,
                "Bàn là hơi nước đứng Philips ComfortTouch GC482 1600W", "ban-la-hoi-nuoc-dung-philips-gc482",
                "Bàn là cây hơi nước đứng công suất 1600W ủi phẳng nếp nhăn nhanh chóng trên mọi loại vải lụa, voan mà không lo cháy sém.",
                "https://images.unsplash.com/photo-1582735689369-4fe89db7114c?w=500",
                new BigDecimal("1990000"), new BigDecimal("1590000"), new BigDecimal("20.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Ba Lan",
                "{\"power\":\"1600W\",\"steam\":\"32g/phút\",\"warranty\":\"24 tháng\"}", new BigDecimal("4.75"), 45);

        // 7. Tạo Sản phẩm Phụ kiện Công nghệ (6 Sản phẩm)
        createProduct(shopTech, catAudio,
                "Tai nghe không dây chống ồn Sony WH-1000XM5 Hi-Res", "tai-nghe-sony-wh-1000xm5",
                "Tai nghe chống ồn chủ động đỉnh cao số 1 thế giới, bộ vi xử lý V1 kép, âm thanh Hi-Res chân thực, thời lượng pin 30 giờ liên tục.",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500",
                new BigDecimal("8490000"), new BigDecimal("7490000"), new BigDecimal("18.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Chính hãng Sony Việt Nam",
                "{\"anc\":\"Chống ồn chủ động kép\",\"battery\":\"30 giờ\",\"bluetooth\":\"5.2\",\"warranty\":\"12 tháng\"}", new BigDecimal("4.98"), 310);

        createProduct(shopTech, catAudio,
                "Tai nghe True Wireless Samsung Galaxy Buds2 Pro", "tai-nghe-galaxy-buds2-pro",
                "Âm thanh Hi-Fi 24-bit sắc nét, chống ồn chủ động thông minh ANC, thiết kế công thái học vừa vặn êm ái khi đeo thể thao.",
                "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500",
                new BigDecimal("3990000"), new BigDecimal("2690000"), new BigDecimal("30.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Chính hãng Samsung",
                "{\"hifi\":\"24bit\",\"waterproof\":\"IPX7\",\"warranty\":\"12 tháng\"}", new BigDecimal("4.88"), 185);

        createProduct(shopTech, catPeripherals,
                "Chuột không dây công thái học Logitech MX Master 3S", "chuot-logitech-mx-master-3s",
                "Cảm biến 8000 DPI lướt mượt trên mọi bề mặt kính, con lăn MagSpeed cuộn 1000 dòng/giây siêu êm ái, kết nối 3 thiết bị cùng lúc.",
                "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500",
                new BigDecimal("2590000"), new BigDecimal("2190000"), new BigDecimal("45.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Logitech Thụy Sĩ",
                "{\"dpi\":\"8000 DPI\",\"clicks\":\"Quiet Clicks\",\"battery\":\"70 ngày\",\"warranty\":\"12 tháng\"}", new BigDecimal("4.95"), 420);

        createProduct(shopTech, catPeripherals,
                "Bàn phím cơ không dây Keychron K2 Pro QMK/VIA RGB", "ban-phim-co-keychron-k2-pro",
                "Layout 75% gọn gàng, switch Gateron Jupiter Pro gõ êm mượt, hỗ trợ tùy biến phím qua QMK/VIA và tương thích hoàn hảo Mac/Windows.",
                "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500",
                new BigDecimal("2250000"), new BigDecimal("1950000"), new BigDecimal("25.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Keychron",
                "{\"switch\":\"Red/Brown/Blue\",\"led\":\"RGB\",\"battery\":\"4000mAh\",\"warranty\":\"12 tháng\"}", new BigDecimal("4.92"), 260);

        createProduct(shopTech, catPower,
                "Pin sạc dự phòng Anker 537 Power Bank 24000mAh 65W PD", "pin-sac-du-phong-anker-537-65w",
                "Dung lượng cực khủng 24000mAh, công suất ra 65W sạc nhanh cùng lúc cho Laptop MacBook, iPhone và iPad một cách tiện lợi.",
                "https://images.unsplash.com/photo-1609592426507-2a133968846d?w=500",
                new BigDecimal("1690000"), new BigDecimal("1390000"), new BigDecimal("50.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Chính hãng Anker",
                "{\"capacity\":\"24000mAh\",\"power\":\"65W Power Delivery\",\"ports\":\"2 Type-C + 1 USB-A\"}", new BigDecimal("4.9"), 178);

        createProduct(shopTech, catPower,
                "Củ sạc nhanh GaN Ugreen Nexode 65W 3 cổng Type-C", "cu-sac-nhanh-gan-ugreen-nexode-65w",
                "Công nghệ GaN bán dẫn thế hệ mới kích thước nhỏ gọn hơn 50%, sạc siêu tốc bảo vệ thông minh chống quá nhiệt cho mọi thiết bị.",
                "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500",
                new BigDecimal("690000"), new BigDecimal("549000"), new BigDecimal("70.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Ugreen",
                "{\"power\":\"65W Max\",\"ports\":\"2 USB-C + 1 USB-A\",\"tech\":\"GaN II\"}", new BigDecimal("4.85"), 315);

        // 8. Tạo Sản phẩm Thời trang & Phong cách (3 Sản phẩm)
        createProduct(shopFashion, catFashionRoot,
                "Áo thun nam Cotton trơn dáng Regular thoáng mát UniStyle", "ao-thun-nam-cotton-tron-unistyle",
                "Chất liệu 100% Cotton chải kỹ thấm hút mồ hôi vượt trội, đường may tỉ mỉ, cổ áo chống bai dão chuẩn phong cách tối giản.",
                "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500",
                new BigDecimal("189000"), new BigDecimal("139000"), new BigDecimal("100.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Việt Nam",
                "{\"material\":\"100% Cotton 220gsm\",\"colors\":\"Trắng, Đen, Xám, Be\",\"sizes\":\"M, L, XL\"}", new BigDecimal("4.8"), 520);

        createProduct(shopFashion, catFashionRoot,
                "Áo khoác gió thể thao trượt nước chống cản gió UniStyle", "ao-khoac-gio-the-thao-unistyle",
                "Chất liệu dù tráng màng PU trượt nước thông minh, cản gió giữ ấm và siêu nhẹ, tiện lợi gấp gọn mang theo mọi cung đường.",
                "https://images.unsplash.com/photo-1548883354-7622d03aca27?w=500",
                new BigDecimal("350000"), new BigDecimal("279000"), new BigDecimal("60.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Việt Nam",
                "{\"feature\":\"Chống nước, cản bụi, có nón\",\"sizes\":\"L, XL, 2XL\"}", new BigDecimal("4.85"), 174);

        createProduct(shopFashion, catFashionRoot,
                "Balo laptop chống sốc chống gù cao cấp UniStyle Oxford", "balo-laptop-chong-soc-unistyle",
                "Vải Oxford 900D chống thấm nước tuyệt đối, ngăn chống sốc đệm tổ ong bảo vệ laptop 15.6 inch, quai đeo công thái học êm vai.",
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500",
                new BigDecimal("490000"), new BigDecimal("389000"), new BigDecimal("40.0"),
                "chiếc", BigDecimal.ONE, BigDecimal.ONE, StorageType.NORMAL, null, "Việt Nam",
                "{\"size\":\"Phù hợp laptop 14 - 15.6 inch\",\"capacity\":\"20L\"}", new BigDecimal("4.9"), 140);
    }

    private void createProduct(Shop shop, Category category, String name, String slug, String description,
                               String thumbnailUrl, BigDecimal originalPrice, BigDecimal sellingPrice,
                               BigDecimal stockQuantity, String unit, BigDecimal stepQuantity,
                               BigDecimal minOrderQuantity, StorageType storageType, String shelfLife,
                               String origin, String attributes, BigDecimal ratingAvg, int reviewCount) {

        if (productRepository.findBySlug(slug).isPresent()) {
            return;
        }

        Product product = Product.builder()
                .shop(shop)
                .category(category)
                .name(name)
                .slug(slug)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .originalPrice(originalPrice)
                .sellingPrice(sellingPrice)
                .stockQuantity(stockQuantity)
                .unit(unit)
                .stepQuantity(stepQuantity)
                .minOrderQuantity(minOrderQuantity)
                .storageType(storageType)
                .shelfLife(shelfLife)
                .origin(origin)
                .attributes(attributes)
                .ratingAvg(ratingAvg)
                .reviewCount(reviewCount)
                .status(ProductStatus.ACTIVE)
                .build();

        // Thêm ảnh phụ
        List<ProductImage> images = new ArrayList<>();
        images.add(ProductImage.builder().product(product).imageUrl(thumbnailUrl).displayOrder(0).build());
        product.setImages(images);

        // Thêm biến thể nếu là hàng tính ký hoặc đồ điện tử
        List<ProductVariant> variants = new ArrayList<>();
        if ("kg".equalsIgnoreCase(unit)) {
            variants.add(ProductVariant.builder().product(product).variantName("Túi 500g").price(sellingPrice.multiply(new BigDecimal("0.5"))).stockQuantity(stockQuantity).build());
            variants.add(ProductVariant.builder().product(product).variantName("Túi 1kg").price(sellingPrice).stockQuantity(stockQuantity).build());
        }
        product.setVariants(variants);

        productRepository.save(product);
    }
}

