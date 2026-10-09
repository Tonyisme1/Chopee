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
import java.time.LocalDateTime;
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
    private final VoucherRepository voucherRepository;
    private final UserAddressRepository userAddressRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.core.env.Environment environment;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        if (java.util.Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return;
        }
        if (userRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu mẫu sàn Chopee Marketplace...");
            seedData();
            log.info("Khởi tạo dữ liệu mẫu thành công!");
        } else {
            log.info("Cơ sở dữ liệu đã có dữ liệu, bỏ qua bước khởi tạo mẫu.");
        }
        seedVouchersIfEmpty();
        seedAddressesIfEmpty();
        refreshSampleMultiTierVariants();
    }

    @Transactional
    public void seedData() {
        String defaultPasswordHash = passwordEncoder.encode("123456");

        // 1. Tạo Tài khoản Người dùng
        User admin = userRepository.save(User.builder()
                .username("admin")
                .passwordHash(defaultPasswordHash)
                .email("admin@chopee.vn")
                .fullName("Quản Trị Viên Chopee")
                .phone("0901234567")
                .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150")
                .role(Role.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        User sellerFood = userRepository.save(User.builder()
                .username("seller_food")
                .passwordHash(defaultPasswordHash)
                .email("dalat_farm@chopee.vn")
                .fullName("Nguyễn Văn Nông")
                .phone("0912345678")
                .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        User sellerDrink = userRepository.save(User.builder()
                .username("seller_drink")
                .passwordHash(defaultPasswordHash)
                .email("hungphat_beverage@chopee.vn")
                .fullName("Trần Hùng Phát")
                .phone("0923456789")
                .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        User sellerAppliance = userRepository.save(User.builder()
                .username("seller_appliances")
                .passwordHash(defaultPasswordHash)
                .email("philips_mall@chopee.vn")
                .fullName("Lê Hoàng Philips")
                .phone("0934567890")
                .avatarUrl("https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        User sellerTech = userRepository.save(User.builder()
                .username("seller_tech")
                .passwordHash(defaultPasswordHash)
                .email("techzone_official@chopee.vn")
                .fullName("Vũ Minh Tech")
                .phone("0945678901")
                .avatarUrl("https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        User sellerFashion = userRepository.save(User.builder()
                .username("seller_fashion")
                .passwordHash(defaultPasswordHash)
                .email("unistyle_fashion@chopee.vn")
                .fullName("Đỗ Thảo Vy")
                .phone("0956789012")
                .avatarUrl("https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        User buyer = userRepository.save(User.builder()
                .username("buyer1")
                .passwordHash(defaultPasswordHash)
                .email("buyer1@chopee.vn")
                .fullName("Hoàng Thị Mua Sắm")
                .phone("0988776655")
                .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build());

        // 2. Tạo 5 Gian hàng (Shops)
        Shop shopFood = shopRepository.save(Shop.builder()
                .user(sellerFood)
                .name("Nông Sản Sạch Đà Lạt")
                .slug("nong-san-sach-da-lat")
                .description("Chuyên cung cấp rau củ quả thủy canh VietGAP tươi ngon, thu hoạch hàng ngày tại nông trường Đà Lạt.")
                .logoUrl("https://images.unsplash.com/photo-1542838132-92c53300491e?w=200")
                .bannerUrl("https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=1200")
                .address("Phường 7, TP. Đà Lạt, Lâm Đồng")
                .phone("0912345678")
                .rating(new BigDecimal("4.9"))
                .shopType(ShopType.FOOD_FRESH)
                .status(ShopStatus.APPROVED)
                .build());

        Shop shopDrink = shopRepository.save(Shop.builder()
                .user(sellerDrink)
                .name("Đại Lý Đồ Uống Hùng Phát")
                .slug("dai-ly-do-uong-hung-phat")
                .description("Phân phối sỉ lẻ bia, nước ngọt, trà đóng chai chính hãng. Cam kết date mới, giao nhanh trong ngày.")
                .logoUrl("https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=200")
                .bannerUrl("https://images.unsplash.com/photo-1527061011665-3652c757a4d4?w=1200")
                .address("Nguyễn Thị Minh Khai, Quận 1, TP. Hồ Chí Minh")
                .phone("0923456789")
                .rating(new BigDecimal("4.8"))
                .shopType(ShopType.GENERAL)
                .status(ShopStatus.APPROVED)
                .build());

        Shop shopAppliance = shopRepository.save(Shop.builder()
                .user(sellerAppliance)
                .name("Thế Giới Gia Dụng Philips")
                .slug("the-gioi-gia-dung-philips")
                .description("Gian hàng chính hãng phân phối thiết bị gia dụng nhà bếp cao cấp Philips, Tefal, Lock&Lock.")
                .logoUrl("https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=200")
                .bannerUrl("https://images.unsplash.com/photo-1556909212-d5b604d0c90d?w=1200")
                .address("Trần Duy Hưng, Cầu Giấy, Hà Nội")
                .phone("0934567890")
                .rating(new BigDecimal("4.95"))
                .shopType(ShopType.OFFICIAL_MALL)
                .status(ShopStatus.APPROVED)
                .build());

        Shop shopTech = shopRepository.save(Shop.builder()
                .user(sellerTech)
                .name("TechZone Official Store")
                .slug("techzone-official-store")
                .description("Cửa hàng công nghệ hàng đầu: tai nghe chống ồn, bàn phím cơ, chuột không dây và phụ kiện sạc cao cấp.")
                .logoUrl("https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=200")
                .bannerUrl("https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200")
                .address("Nguyễn Trãi, Thanh Xuân, Hà Nội")
                .phone("0945678901")
                .rating(new BigDecimal("4.85"))
                .shopType(ShopType.OFFICIAL_MALL)
                .status(ShopStatus.APPROVED)
                .build());

        Shop shopFashion = shopRepository.save(Shop.builder()
                .user(sellerFashion)
                .name("UniStyle - Thời Trang & Phụ Kiện")
                .slug("unistyle-thoi-trang")
                .description("Thương hiệu thời trang basic tối giản, áo thun cotton thoáng mát, áo khoác cản gió và balo tiện ích.")
                .logoUrl("https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=200")
                .bannerUrl("https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1200")
                .address("Chùa Bộc, Đống Đa, Hà Nội")
                .phone("0956789012")
                .rating(new BigDecimal("4.75"))
                .shopType(ShopType.GENERAL)
                .status(ShopStatus.APPROVED)
                .build());

        // 3. Tạo Cây Danh mục Sản phẩm (Categories)
        Category catFoodRoot = categoryRepository.save(Category.builder()
                .name("Thực phẩm tươi sống")
                .slug("thuc-pham-tuoi-song")
                .iconUrl("🥬")
                .displayOrder(1)
                .build());

        Category catVeggies = categoryRepository.save(Category.builder()
                .name("Rau củ quả tươi")
                .slug("rau-cu-qua-tuoi")
                .parent(catFoodRoot)
                .iconUrl("🥕")
                .displayOrder(1)
                .build());

        Category catMeatFish = categoryRepository.save(Category.builder()
                .name("Thịt & Thủy hải sản")
                .slug("thit-thuy-hai-san")
                .parent(catFoodRoot)
                .iconUrl("🐟")
                .displayOrder(2)
                .build());

        Category catDrinkRoot = categoryRepository.save(Category.builder()
                .name("Đồ uống & Giải khát")
                .slug("do-uong-giai-khat")
                .iconUrl("🥤")
                .displayOrder(2)
                .build());

        Category catBeer = categoryRepository.save(Category.builder()
                .name("Bia & Đồ uống có cồn")
                .slug("bia-do-uong-co-con")
                .parent(catDrinkRoot)
                .iconUrl("🍺")
                .displayOrder(1)
                .build());

        Category catSoftDrink = categoryRepository.save(Category.builder()
                .name("Nước ngọt & Trà giải nhiệt")
                .slug("nuoc-ngot-tra-giai-nhiet")
                .parent(catDrinkRoot)
                .iconUrl("🧃")
                .displayOrder(2)
                .build());

        Category catApplianceRoot = categoryRepository.save(Category.builder()
                .name("Thiết bị gia dụng")
                .slug("thiet-bi-gia-dung")
                .iconUrl("🍳")
                .displayOrder(3)
                .build());

        Category catKitchen = categoryRepository.save(Category.builder()
                .name("Nồi chiên & Bếp điện")
                .slug("noi-chien-bep-dien")
                .parent(catApplianceRoot)
                .iconUrl("🥘")
                .displayOrder(1)
                .build());

        Category catBlender = categoryRepository.save(Category.builder()
                .name("Máy xay & Máy ép")
                .slug("may-xay-may-ep")
                .parent(catApplianceRoot)
                .iconUrl("🍹")
                .displayOrder(2)
                .build());

        Category catTechRoot = categoryRepository.save(Category.builder()
                .name("Phụ kiện công nghệ")
                .slug("phu-kien-cong-nghe")
                .iconUrl("🎧")
                .displayOrder(4)
                .build());

        Category catAudio = categoryRepository.save(Category.builder()
                .name("Tai nghe & Loa")
                .slug("tai-nghe-loa")
                .parent(catTechRoot)
                .iconUrl("🔊")
                .displayOrder(1)
                .build());

        Category catPeripherals = categoryRepository.save(Category.builder()
                .name("Bàn phím & Chuột")
                .slug("ban-phim-chuot")
                .parent(catTechRoot)
                .iconUrl("⌨️")
                .displayOrder(2)
                .build());

        Category catPower = categoryRepository.save(Category.builder()
                .name("Pin sạc & Cáp dữ liệu")
                .slug("pin-sac-cap-du-lieu")
                .parent(catTechRoot)
                .iconUrl("🔋")
                .displayOrder(3)
                .build());

        Category catFashionRoot = categoryRepository.save(Category.builder()
                .name("Thời trang & Phong cách")
                .slug("thoi-trang-phong-cach")
                .iconUrl("👕")
                .displayOrder(5)
                .build());

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

        // Thêm biến thể nếu là hàng tính ký, đồ thời trang hoặc công nghệ
        List<ProductVariant> variants = new ArrayList<>();
        if ("kg".equalsIgnoreCase(unit)) {
            product.setTierVariation("[{\"name\":\"Quy cách đóng gói\",\"options\":[\"Túi 500g (0.5kg)\",\"Túi 1.0 kg (1 ký)\",\"Túi 2.0 kg\"]}]");
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Túi 500g (0.5kg)")
                    .sku(slug.toUpperCase() + "-500G")
                    .attributes("{\"Quy cách đóng gói\":\"Túi 500g (0.5kg)\"}")
                    .price(sellingPrice.multiply(new BigDecimal("0.5")))
                    .stockQuantity(stockQuantity)
                    .build());
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Túi 1.0 kg (1 ký)")
                    .sku(slug.toUpperCase() + "-1KG")
                    .attributes("{\"Quy cách đóng gói\":\"Túi 1.0 kg (1 ký)\"}")
                    .price(sellingPrice)
                    .stockQuantity(stockQuantity)
                    .build());
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Túi 2.0 kg")
                    .sku(slug.toUpperCase() + "-2KG")
                    .attributes("{\"Quy cách đóng gói\":\"Túi 2.0 kg\"}")
                    .price(sellingPrice.multiply(new BigDecimal("2.0")))
                    .stockQuantity(stockQuantity)
                    .build());
        } else if (slug.contains("ban-phim-co")) {
            product.setTierVariation("[{\"name\":\"Loại Switch\",\"options\":[\"Switch Red\",\"Switch Brown\",\"Switch Blue\"]}]");
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Switch Red")
                    .sku("K2PRO-RED")
                    .attributes("{\"Loại Switch\":\"Switch Red\"}")
                    .price(sellingPrice)
                    .stockQuantity(new BigDecimal("15"))
                    .build());
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Switch Brown")
                    .sku("K2PRO-BROWN")
                    .attributes("{\"Loại Switch\":\"Switch Brown\"}")
                    .price(sellingPrice)
                    .stockQuantity(new BigDecimal("10"))
                    .build());
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Switch Blue")
                    .sku("K2PRO-BLUE")
                    .attributes("{\"Loại Switch\":\"Switch Blue\"}")
                    .price(sellingPrice)
                    .stockQuantity(new BigDecimal("10"))
                    .build());
        } else if (slug.contains("ao-thun-nam")) {
            product.setTierVariation("[{\"name\":\"Màu sắc\",\"options\":[\"Trắng\",\"Đen\"]},{\"name\":\"Size\",\"options\":[\"M\",\"L\"]}]");
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Trắng - Size M")
                    .sku("UNISTYLE-WHITE-M")
                    .attributes("{\"Màu sắc\":\"Trắng\",\"Size\":\"M\"}")
                    .price(sellingPrice)
                    .stockQuantity(new BigDecimal("25"))
                    .build());
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Trắng - Size L")
                    .sku("UNISTYLE-WHITE-L")
                    .attributes("{\"Màu sắc\":\"Trắng\",\"Size\":\"L\"}")
                    .price(sellingPrice)
                    .stockQuantity(new BigDecimal("25"))
                    .build());
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Đen - Size M")
                    .sku("UNISTYLE-BLACK-M")
                    .attributes("{\"Màu sắc\":\"Đen\",\"Size\":\"M\"}")
                    .price(sellingPrice)
                    .stockQuantity(new BigDecimal("25"))
                    .build());
            variants.add(ProductVariant.builder()
                    .product(product)
                    .variantName("Đen - Size L")
                    .sku("UNISTYLE-BLACK-L")
                    .attributes("{\"Màu sắc\":\"Đen\",\"Size\":\"L\"}")
                    .price(sellingPrice)
                    .stockQuantity(new BigDecimal("25"))
                    .build());
        }
        product.setVariants(variants);

        productRepository.save(product);
    }

    @Transactional
    public void seedVouchersIfEmpty() {
        if (voucherRepository.count() == 0) {
            log.info("Khởi tạo mã giảm giá mẫu (Vouchers)...");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = now.plusMonths(6);

            // Voucher sàn
            voucherRepository.save(Voucher.builder()
                    .code("CHOPEE10K")
                    .discountType(DiscountType.FIXED_AMOUNT)
                    .discountValue(new BigDecimal("10000"))
                    .minOrderAmount(new BigDecimal("50000"))
                    .usageLimit(1000)
                    .usedCount(5)
                    .startDate(now.minusDays(1))
                    .endDate(endDate)
                    .isDeleted(false)
                    .build());

            voucherRepository.save(Voucher.builder()
                    .code("FREESHIPCHO")
                    .discountType(DiscountType.FIXED_AMOUNT)
                    .discountValue(new BigDecimal("15000"))
                    .minOrderAmount(new BigDecimal("100000"))
                    .usageLimit(2000)
                    .usedCount(12)
                    .startDate(now.minusDays(1))
                    .endDate(endDate)
                    .isDeleted(false)
                    .build());

            // Voucher của shop Đà Lạt Farm
            shopRepository.findBySlug("nong-san-sach-da-lat").ifPresent(shop -> {
                voucherRepository.save(Voucher.builder()
                        .code("DALATFARM20")
                        .shop(shop)
                        .discountType(DiscountType.PERCENT)
                        .discountValue(new BigDecimal("20"))
                        .minOrderAmount(new BigDecimal("150000"))
                        .maxDiscountAmount(new BigDecimal("30000"))
                        .usageLimit(500)
                        .usedCount(8)
                        .startDate(now.minusDays(1))
                        .endDate(endDate)
                        .isDeleted(false)
                        .build());
            });

            // Voucher của shop TechZone
            shopRepository.findBySlug("techzone-official-store").ifPresent(shop -> {
                voucherRepository.save(Voucher.builder()
                        .code("TECHSALE50")
                        .shop(shop)
                        .discountType(DiscountType.FIXED_AMOUNT)
                        .discountValue(new BigDecimal("50000"))
                        .minOrderAmount(new BigDecimal("300000"))
                        .usageLimit(200)
                        .usedCount(2)
                        .startDate(now.minusDays(1))
                        .endDate(endDate)
                        .isDeleted(false)
                        .build());
            });
            log.info("Khởi tạo mã giảm giá mẫu thành công!");
        }
    }

    @Transactional
    public void seedAddressesIfEmpty() {
        if (userAddressRepository.count() == 0) {
            userRepository.findByUsername("buyer1").ifPresent(buyer -> {
                userAddressRepository.save(UserAddress.builder()
                        .user(buyer)
                        .receiverName("Nguyễn Mua Sắm")
                        .phone("0987654321")
                        .province("TP. Hồ Chí Minh")
                        .district("Quận 1")
                        .ward("Phường Bến Nghé")
                        .detailAddress("123 Lê Lợi")
                        .isDefault(true)
                        .build());

                userAddressRepository.save(UserAddress.builder()
                        .user(buyer)
                        .receiverName("Nguyễn Mua Sắm (Cơ quan)")
                        .phone("0987654321")
                        .province("TP. Hồ Chí Minh")
                        .district("Quận Bình Thạnh")
                        .ward("Phường 22")
                        .detailAddress("Tòa nhà Landmark 81, 720A Điện Biên Phủ")
                        .isDefault(false)
                        .build());
            });
        }
    }

    @Transactional
    public void refreshSampleMultiTierVariants() {
        log.info("Khởi tạo và làm mới ma trận phân loại hàng đa tầng (Multi-tier Variant Matrix) chuẩn UTF-8...");

        try {
            jdbcTemplate.update("DELETE FROM cart_items WHERE variant_id IS NOT NULL");
            jdbcTemplate.update("UPDATE order_items SET variant_id = NULL WHERE variant_id IS NOT NULL");
        } catch (Exception e) {
            log.warn("Không thể dọn dẹp ràng buộc khóa ngoại variant: {}", e.getMessage());
        }

        // 1. Cà chua beef Đà Lạt
        productRepository.findBySlug("ca-chua-beef-da-lat").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Loại\",\"options\":[\"Tươi ngon\",\"Sấy dẻo / Khô\"]},{\"name\":\"Quy cách đóng gói\",\"options\":[\"Túi 500g\",\"Túi 1.0 kg\",\"Túi 2.0 kg\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("14000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Tươi ngon - Túi 500g", "CACHUA-TUOI-500G", "{\"Loại\":\"Tươi ngon\",\"Quy cách đóng gói\":\"Túi 500g\"}", new BigDecimal("14000"), new BigDecimal("100")));
            vars.add(createVariant(p, "Tươi ngon - Túi 1.0 kg", "CACHUA-TUOI-1KG", "{\"Loại\":\"Tươi ngon\",\"Quy cách đóng gói\":\"Túi 1.0 kg\"}", new BigDecimal("28000"), new BigDecimal("100")));
            vars.add(createVariant(p, "Tươi ngon - Túi 2.0 kg", "CACHUA-TUOI-2KG", "{\"Loại\":\"Tươi ngon\",\"Quy cách đóng gói\":\"Túi 2.0 kg\"}", new BigDecimal("55000"), new BigDecimal("80")));
            vars.add(createVariant(p, "Sấy dẻo / Khô - Túi 500g", "CACHUA-KHO-500G", "{\"Loại\":\"Sấy dẻo / Khô\",\"Quy cách đóng gói\":\"Túi 500g\"}", new BigDecimal("35000"), new BigDecimal("50")));
            vars.add(createVariant(p, "Sấy dẻo / Khô - Túi 1.0 kg", "CACHUA-KHO-1KG", "{\"Loại\":\"Sấy dẻo / Khô\",\"Quy cách đóng gói\":\"Túi 1.0 kg\"}", new BigDecimal("68000"), new BigDecimal("50")));
            vars.add(createVariant(p, "Sấy dẻo / Khô - Túi 2.0 kg", "CACHUA-KHO-2KG", "{\"Loại\":\"Sấy dẻo / Khô\",\"Quy cách đóng gói\":\"Túi 2.0 kg\"}", new BigDecimal("130000"), new BigDecimal("40")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 2. Dưa leo baby
        productRepository.findBySlug("dua-leo-baby-gion-ngot").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Loại\",\"options\":[\"Tươi giòn\",\"Ngâm chua ngọt\"]},{\"name\":\"Quy cách đóng gói\",\"options\":[\"Túi 500g\",\"Túi 1.0 kg\",\"Túi 2.0 kg\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("12000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Tươi giòn - Túi 500g", "DUALEO-TUOI-500G", "{\"Loại\":\"Tươi giòn\",\"Quy cách đóng gói\":\"Túi 500g\"}", new BigDecimal("12000"), new BigDecimal("80")));
            vars.add(createVariant(p, "Tươi giòn - Túi 1.0 kg", "DUALEO-TUOI-1KG", "{\"Loại\":\"Tươi giòn\",\"Quy cách đóng gói\":\"Túi 1.0 kg\"}", new BigDecimal("24000"), new BigDecimal("80")));
            vars.add(createVariant(p, "Tươi giòn - Túi 2.0 kg", "DUALEO-TUOI-2KG", "{\"Loại\":\"Tươi giòn\",\"Quy cách đóng gói\":\"Túi 2.0 kg\"}", new BigDecimal("46000"), new BigDecimal("60")));
            vars.add(createVariant(p, "Ngâm chua ngọt - Túi 500g", "DUALEO-CHUA-500G", "{\"Loại\":\"Ngâm chua ngọt\",\"Quy cách đóng gói\":\"Túi 500g\"}", new BigDecimal("22000"), new BigDecimal("40")));
            vars.add(createVariant(p, "Ngâm chua ngọt - Túi 1.0 kg", "DUALEO-CHUA-1KG", "{\"Loại\":\"Ngâm chua ngọt\",\"Quy cách đóng gói\":\"Túi 1.0 kg\"}", new BigDecimal("42000"), new BigDecimal("40")));
            vars.add(createVariant(p, "Ngâm chua ngọt - Túi 2.0 kg", "DUALEO-CHUA-2KG", "{\"Loại\":\"Ngâm chua ngọt\",\"Quy cách đóng gói\":\"Túi 2.0 kg\"}", new BigDecimal("80000"), new BigDecimal("30")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 3. Thịt ba chỉ heo sạch chuẩn CP
        productRepository.findBySlug("thit-ba-chi-heo-sach-cp").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Phân loại thịt\",\"options\":[\"Ba chỉ rút sườn\",\"Sườn non heo\",\"Nạc dăm\"]},{\"name\":\"Khối lượng khay\",\"options\":[\"Khay 300g\",\"Khay 500g\",\"Khay 1.0 kg\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("40000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Ba chỉ rút sườn - Khay 300g", "HEO-BACHI-300G", "{\"Phân loại thịt\":\"Ba chỉ rút sườn\",\"Khối lượng khay\":\"Khay 300g\"}", new BigDecimal("45000"), new BigDecimal("50")));
            vars.add(createVariant(p, "Ba chỉ rút sườn - Khay 500g", "HEO-BACHI-500G", "{\"Phân loại thịt\":\"Ba chỉ rút sườn\",\"Khối lượng khay\":\"Khay 500g\"}", new BigDecimal("75000"), new BigDecimal("50")));
            vars.add(createVariant(p, "Ba chỉ rút sườn - Khay 1.0 kg", "HEO-BACHI-1KG", "{\"Phân loại thịt\":\"Ba chỉ rút sườn\",\"Khối lượng khay\":\"Khay 1.0 kg\"}", new BigDecimal("145000"), new BigDecimal("40")));
            vars.add(createVariant(p, "Sườn non heo - Khay 300g", "HEO-SUON-300G", "{\"Phân loại thịt\":\"Sườn non heo\",\"Khối lượng khay\":\"Khay 300g\"}", new BigDecimal("55000"), new BigDecimal("40")));
            vars.add(createVariant(p, "Sườn non heo - Khay 500g", "HEO-SUON-500G", "{\"Phân loại thịt\":\"Sườn non heo\",\"Khối lượng khay\":\"Khay 500g\"}", new BigDecimal("90000"), new BigDecimal("40")));
            vars.add(createVariant(p, "Sườn non heo - Khay 1.0 kg", "HEO-SUON-1KG", "{\"Phân loại thịt\":\"Sườn non heo\",\"Khối lượng khay\":\"Khay 1.0 kg\"}", new BigDecimal("175000"), new BigDecimal("30")));
            vars.add(createVariant(p, "Nạc dăm - Khay 300g", "HEO-NAC-300G", "{\"Phân loại thịt\":\"Nạc dăm\",\"Khối lượng khay\":\"Khay 300g\"}", new BigDecimal("40000"), new BigDecimal("40")));
            vars.add(createVariant(p, "Nạc dăm - Khay 500g", "HEO-NAC-500G", "{\"Phân loại thịt\":\"Nạc dăm\",\"Khối lượng khay\":\"Khay 500g\"}", new BigDecimal("65000"), new BigDecimal("40")));
            vars.add(createVariant(p, "Nạc dăm - Khay 1.0 kg", "HEO-NAC-1KG", "{\"Phân loại thịt\":\"Nạc dăm\",\"Khối lượng khay\":\"Khay 1.0 kg\"}", new BigDecimal("125000"), new BigDecimal("30")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 4. Bàn phím cơ Keychron K2 Pro
        productRepository.findBySlug("ban-phim-co-keychron-k2-pro").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Layout\",\"options\":[\"Layout 75%\",\"TKL 87 phím\"]},{\"name\":\"Switch\",\"options\":[\"Red Switch (Êm)\",\"Brown Switch (Khấc)\",\"Blue Switch (Clicky)\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("1650000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Layout 75% - Red Switch (Êm)", "K2PRO-75-RED", "{\"Layout\":\"Layout 75%\",\"Switch\":\"Red Switch (Êm)\"}", new BigDecimal("1650000"), new BigDecimal("25")));
            vars.add(createVariant(p, "Layout 75% - Brown Switch (Khấc)", "K2PRO-75-BROWN", "{\"Layout\":\"Layout 75%\",\"Switch\":\"Brown Switch (Khấc)\"}", new BigDecimal("1650000"), new BigDecimal("20")));
            vars.add(createVariant(p, "Layout 75% - Blue Switch (Clicky)", "K2PRO-75-BLUE", "{\"Layout\":\"Layout 75%\",\"Switch\":\"Blue Switch (Clicky)\"}", new BigDecimal("1650000"), new BigDecimal("15")));
            vars.add(createVariant(p, "TKL 87 phím - Red Switch (Êm)", "K2PRO-87-RED", "{\"Layout\":\"TKL 87 phím\",\"Switch\":\"Red Switch (Êm)\"}", new BigDecimal("1750000"), new BigDecimal("20")));
            vars.add(createVariant(p, "TKL 87 phím - Brown Switch (Khấc)", "K2PRO-87-BROWN", "{\"Layout\":\"TKL 87 phím\",\"Switch\":\"Brown Switch (Khấc)\"}", new BigDecimal("1750000"), new BigDecimal("15")));
            vars.add(createVariant(p, "TKL 87 phím - Blue Switch (Clicky)", "K2PRO-87-BLUE", "{\"Layout\":\"TKL 87 phím\",\"Switch\":\"Blue Switch (Clicky)\"}", new BigDecimal("1750000"), new BigDecimal("10")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 5. Áo thun nam UniStyle
        productRepository.findBySlug("ao-thun-nam-cotton-tron-unistyle").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Màu sắc\",\"options\":[\"Trắng Basic\",\"Đen Tuyền\",\"Xám Tiêu\"]},{\"name\":\"Kích cỡ\",\"options\":[\"Size M\",\"Size L\",\"Size XL\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("180000"));
            List<ProductVariant> vars = new ArrayList<>();
            for (String color : new String[]{"Trắng Basic", "Đen Tuyền", "Xám Tiêu"}) {
                for (String size : new String[]{"Size M", "Size L", "Size XL"}) {
                    String sku = "UNISTYLE-" + (color.contains("Trắng") ? "W" : color.contains("Đen") ? "B" : "G") + "-" + size.replace("Size ", "");
                    vars.add(createVariant(p, color + " - " + size, sku, "{\"Màu sắc\":\"" + color + "\",\"Kích cỡ\":\"" + size + "\"}", new BigDecimal("180000"), new BigDecimal("30")));
                }
            }
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 6. Tai nghe Sony WH-1000XM5
        productRepository.findBySlug("tai-nghe-sony-wh-1000xm5").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Màu sắc\",\"options\":[\"Đen Nhám (Matte Black)\",\"Bạc Ánh Kim (Silver)\",\"Xanh Navy (Midnight Blue)\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("7990000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Đen Nhám (Matte Black)", "WH1000XM5-BLACK", "{\"Màu sắc\":\"Đen Nhám (Matte Black)\"}", new BigDecimal("7990000"), new BigDecimal("30")));
            vars.add(createVariant(p, "Bạc Ánh Kim (Silver)", "WH1000XM5-SILVER", "{\"Màu sắc\":\"Bạc Ánh Kim (Silver)\"}", new BigDecimal("7990000"), new BigDecimal("25")));
            vars.add(createVariant(p, "Xanh Navy (Midnight Blue)", "WH1000XM5-NAVY", "{\"Màu sắc\":\"Xanh Navy (Midnight Blue)\"}", new BigDecimal("8190000"), new BigDecimal("15")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 7. Thùng Bia Tiger Crystal 330ml
        productRepository.findBySlug("thung-24-lon-bia-tiger-crystal").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Quy cách đóng gói\",\"options\":[\"Lon lẻ 330ml\",\"Lốc 6 lon\",\"Thùng 24 lon\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("17000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Lon lẻ 330ml", "TIGER-LON-330", "{\"Quy cách đóng gói\":\"Lon lẻ 330ml\"}", new BigDecimal("17000"), new BigDecimal("300")));
            vars.add(createVariant(p, "Lốc 6 lon", "TIGER-LOC-6", "{\"Quy cách đóng gói\":\"Lốc 6 lon\"}", new BigDecimal("99000"), new BigDecimal("150")));
            vars.add(createVariant(p, "Thùng 24 lon", "TIGER-THUNG-24", "{\"Quy cách đóng gói\":\"Thùng 24 lon\"}", new BigDecimal("385000"), new BigDecimal("100")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 8. Nồi chiên không dầu Philips HD9252
        productRepository.findBySlug("noi-chien-khong-dau-philips-hd9252").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Phiên bản dung tích\",\"options\":[\"Bản 4.1L (Gia đình nhỏ)\",\"Bản 6.2L XXL (Gia đình lớn)\"]},{\"name\":\"Màu sắc\",\"options\":[\"Đen bóng\",\"Trắng ngọc trai\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("1850000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Bản 4.1L (Gia đình nhỏ) - Đen bóng", "PHILIPS-41L-BLK", "{\"Phiên bản dung tích\":\"Bản 4.1L (Gia đình nhỏ)\",\"Màu sắc\":\"Đen bóng\"}", new BigDecimal("1850000"), new BigDecimal("25")));
            vars.add(createVariant(p, "Bản 4.1L (Gia đình nhỏ) - Trắng ngọc trai", "PHILIPS-41L-WHT", "{\"Phiên bản dung tích\":\"Bản 4.1L (Gia đình nhỏ)\",\"Màu sắc\":\"Trắng ngọc trai\"}", new BigDecimal("1950000"), new BigDecimal("20")));
            vars.add(createVariant(p, "Bản 6.2L XXL (Gia đình lớn) - Đen bóng", "PHILIPS-62L-BLK", "{\"Phiên bản dung tích\":\"Bản 6.2L XXL (Gia đình lớn)\",\"Màu sắc\":\"Đen bóng\"}", new BigDecimal("2850000"), new BigDecimal("20")));
            vars.add(createVariant(p, "Bản 6.2L XXL (Gia đình lớn) - Trắng ngọc trai", "PHILIPS-62L-WHT", "{\"Phiên bản dung tích\":\"Bản 6.2L XXL (Gia đình lớn)\",\"Màu sắc\":\"Trắng ngọc trai\"}", new BigDecimal("2950000"), new BigDecimal("15")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 9. Thêm hoặc Cập nhật Gạo ST25 Ông Cua
        shopRepository.findBySlug("nong-san-sach-da-lat").ifPresent(shop -> {
            categoryRepository.findBySlug("rau-cu-qua-tuoi").ifPresent(cat -> {
                Product gao = productRepository.findBySlug("gao-st25-ong-cua-thuong-hang")
                        .orElseGet(() -> Product.builder()
                                .shop(shop)
                                .category(cat)
                                .slug("gao-st25-ong-cua-thuong-hang")
                                .build());

                gao.setShop(shop);
                gao.setCategory(cat);
                gao.setName("Gạo ST25 Ông Cua Thượng Hạng Chuẩn Gạo Ngon Thế Giới");
                gao.setDescription("Gạo ST25 đạt giải gạo ngon nhất thế giới. Hạt thon dài, trắng trong, dẻo thơm mùi lá dứa tự nhiên dù để nguội.");
                gao.setThumbnailUrl("https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500");
                gao.setOriginalPrice(new BigDecimal("220000"));
                gao.setSellingPrice(new BigDecimal("38000"));
                gao.setStockQuantity(new BigDecimal("500"));
                gao.setSoldQuantity(new BigDecimal("142"));
                gao.setUnit("bao");
                gao.setStepQuantity(BigDecimal.ONE);
                gao.setMinOrderQuantity(BigDecimal.ONE);
                gao.setStorageType(StorageType.NORMAL);
                gao.setShelfLife("12 tháng");
                gao.setOrigin("Sóc Trăng, Việt Nam");
                gao.setAttributes("{\"cert\":\"VietGAP, Chuẩn Quốc Tế\",\"origin\":\"Sóc Trăng\"}");
                gao.setTierVariation("[{\"name\":\"Loại gạo\",\"options\":[\"ST25 Lúa Tôm Thượng Hạng\",\"Gạo Lứt Đỏ ST25\"]},{\"name\":\"Quy cách đóng gói\",\"options\":[\"Túi 1.0 kg\",\"Bao 5.0 kg\",\"Bao 10 kg\",\"Bao 25 kg\"]}]");
                gao.setHasVariants(true);
                gao.setRatingAvg(new BigDecimal("5.0"));
                gao.setReviewCount(210);
                gao.setStatus(ProductStatus.ACTIVE);

                if (gao.getImages() == null) {
                    gao.setImages(new ArrayList<>());
                }
                if (gao.getImages().isEmpty()) {
                    gao.getImages().add(ProductImage.builder().product(gao).imageUrl("https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500").displayOrder(0).build());
                }

                List<ProductVariant> vars = new ArrayList<>();
                vars.add(createVariant(gao, "ST25 Lúa Tôm Thượng Hạng - Túi 1.0 kg", "ST25-TOM-1KG", "{\"Loại gạo\":\"ST25 Lúa Tôm Thượng Hạng\",\"Quy cách đóng gói\":\"Túi 1.0 kg\"}", new BigDecimal("38000"), new BigDecimal("100")));
                vars.add(createVariant(gao, "ST25 Lúa Tôm Thượng Hạng - Bao 5.0 kg", "ST25-TOM-5KG", "{\"Loại gạo\":\"ST25 Lúa Tôm Thượng Hạng\",\"Quy cách đóng gói\":\"Bao 5.0 kg\"}", new BigDecimal("180000"), new BigDecimal("100")));
                vars.add(createVariant(gao, "ST25 Lúa Tôm Thượng Hạng - Bao 10 kg", "ST25-TOM-10KG", "{\"Loại gạo\":\"ST25 Lúa Tôm Thượng Hạng\",\"Quy cách đóng gói\":\"Bao 10 kg\"}", new BigDecimal("350000"), new BigDecimal("80")));
                vars.add(createVariant(gao, "ST25 Lúa Tôm Thượng Hạng - Bao 25 kg", "ST25-TOM-25KG", "{\"Loại gạo\":\"ST25 Lúa Tôm Thượng Hạng\",\"Quy cách đóng gói\":\"Bao 25 kg\"}", new BigDecimal("850000"), new BigDecimal("50")));
                vars.add(createVariant(gao, "Gạo Lứt Đỏ ST25 - Túi 1.0 kg", "ST25-LUT-1KG", "{\"Loại gạo\":\"Gạo Lứt Đỏ ST25\",\"Quy cách đóng gói\":\"Túi 1.0 kg\"}", new BigDecimal("42000"), new BigDecimal("80")));
                vars.add(createVariant(gao, "Gạo Lứt Đỏ ST25 - Bao 5.0 kg", "ST25-LUT-5KG", "{\"Loại gạo\":\"Gạo Lứt Đỏ ST25\",\"Quy cách đóng gói\":\"Bao 5.0 kg\"}", new BigDecimal("200000"), new BigDecimal("80")));
                vars.add(createVariant(gao, "Gạo Lứt Đỏ ST25 - Bao 10 kg", "ST25-LUT-10KG", "{\"Loại gạo\":\"Gạo Lứt Đỏ ST25\",\"Quy cách đóng gói\":\"Bao 10 kg\"}", new BigDecimal("390000"), new BigDecimal("60")));
                vars.add(createVariant(gao, "Gạo Lứt Đỏ ST25 - Bao 25 kg", "ST25-LUT-25KG", "{\"Loại gạo\":\"Gạo Lứt Đỏ ST25\",\"Quy cách đóng gói\":\"Bao 25 kg\"}", new BigDecimal("950000"), new BigDecimal("40")));
                gao.getVariants().clear();
                gao.getVariants().addAll(vars);
                productRepository.save(gao);
            });
        });

        // 10. Thêm hoặc Cập nhật iPhone 15 Pro Max
        shopRepository.findBySlug("techzone-official-store").ifPresent(shop -> {
            categoryRepository.findBySlug("phu-kien-cong-nghe").ifPresent(cat -> {
                Product phone = productRepository.findBySlug("iphone-15-pro-max-vna")
                        .orElseGet(() -> Product.builder()
                                .shop(shop)
                                .category(cat)
                                .slug("iphone-15-pro-max-vna")
                                .build());

                phone.setShop(shop);
                phone.setCategory(cat);
                phone.setName("Điện Thoại iPhone 15 Pro Max 5G Chính Hãng Apple VN/A");
                phone.setDescription("Khung viền Titan chuẩn hàng không vũ trụ, chip A17 Pro mạnh mẽ vượt trội, camera zoom quang học 5x sắc nét đỉnh cao.");
                phone.setThumbnailUrl("https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500");
                phone.setOriginalPrice(new BigDecimal("34990000"));
                phone.setSellingPrice(new BigDecimal("24990000"));
                phone.setStockQuantity(new BigDecimal("150"));
                phone.setSoldQuantity(new BigDecimal("86"));
                phone.setUnit("chiếc");
                phone.setStepQuantity(BigDecimal.ONE);
                phone.setMinOrderQuantity(BigDecimal.ONE);
                phone.setStorageType(StorageType.NORMAL);
                phone.setShelfLife("12 tháng bảo hành chính hãng");
                phone.setOrigin("Chính Hãng Apple VN/A");
                phone.setAttributes("{\"chip\":\"Apple A17 Pro\",\"screen\":\"6.7 inch Super Retina XDR OLED\",\"origin\":\"Chính Hãng Apple VN/A\"}");
                phone.setTierVariation("[{\"name\":\"Màu sắc\",\"options\":[\"Titan Tự Nhiên\",\"Đen Midnight\",\"Trắng Starlight\"]},{\"name\":\"Cấu hình RAM/ROM\",\"options\":[\"256GB\",\"512GB\",\"1TB\"]}]");
                phone.setHasVariants(true);
                phone.setRatingAvg(new BigDecimal("5.0"));
                phone.setReviewCount(450);
                phone.setStatus(ProductStatus.ACTIVE);

                if (phone.getImages() == null) {
                    phone.setImages(new ArrayList<>());
                }
                if (phone.getImages().isEmpty()) {
                    phone.getImages().add(ProductImage.builder().product(phone).imageUrl("https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500").displayOrder(0).build());
                }

                List<ProductVariant> vars = new ArrayList<>();
                for (String color : new String[]{"Titan Tự Nhiên", "Đen Midnight", "Trắng Starlight"}) {
                    for (String cap : new String[]{"256GB", "512GB", "1TB"}) {
                        BigDecimal pr = cap.equals("256GB") ? new BigDecimal("24990000") : cap.equals("512GB") ? new BigDecimal("29990000") : new BigDecimal("34990000");
                        String sku = "IP15-" + (color.contains("Titan") ? "NAT" : color.contains("Đen") ? "BLK" : "WHT") + "-" + cap;
                        vars.add(createVariant(phone, color + " - " + cap, sku, "{\"Màu sắc\":\"" + color + "\",\"Cấu hình RAM/ROM\":\"" + cap + "\"}", pr, new BigDecimal("25")));
                    }
                }
                phone.getVariants().clear();
                phone.getVariants().addAll(vars);
                productRepository.save(phone);
            });
        });

        // 11. Tôm sú tươi sống Cà Mau
        productRepository.findBySlug("tom-su-tuoi-song-ca-mau").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Quy cách bảo quản\",\"options\":[\"Tôm sống bơi oxy\",\"Cấp đông nguyên con\"]},{\"name\":\"Khối lượng đóng gói\",\"options\":[\"Hộp 500g (12-14 con)\",\"Hộp 1.0 kg (25-28 con)\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("120000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Tôm sống bơi oxy - Hộp 500g (12-14 con)", "TOMSU-LIVE-500G", "{\"Quy cách bảo quản\":\"Tôm sống bơi oxy\",\"Khối lượng đóng gói\":\"Hộp 500g (12-14 con)\"}", new BigDecimal("135000"), new BigDecimal("40")));
            vars.add(createVariant(p, "Tôm sống bơi oxy - Hộp 1.0 kg (25-28 con)", "TOMSU-LIVE-1KG", "{\"Quy cách bảo quản\":\"Tôm sống bơi oxy\",\"Khối lượng đóng gói\":\"Hộp 1.0 kg (25-28 con)\"}", new BigDecimal("260000"), new BigDecimal("30")));
            vars.add(createVariant(p, "Cấp đông nguyên con - Hộp 500g (12-14 con)", "TOMSU-FROZEN-500G", "{\"Quy cách bảo quản\":\"Cấp đông nguyên con\",\"Khối lượng đóng gói\":\"Hộp 500g (12-14 con)\"}", new BigDecimal("120000"), new BigDecimal("50")));
            vars.add(createVariant(p, "Cấp đông nguyên con - Hộp 1.0 kg (25-28 con)", "TOMSU-FROZEN-1KG", "{\"Quy cách bảo quản\":\"Cấp đông nguyên con\",\"Khối lượng đóng gói\":\"Hộp 1.0 kg (25-28 con)\"}", new BigDecimal("230000"), new BigDecimal("40")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 12. Chuột không dây Logitech MX Master 3S
        productRepository.findBySlug("chuot-logitech-mx-master-3s").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Màu sắc\",\"options\":[\"Đen Xám (Graphite)\",\"Trắng Xám (Pale Grey)\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("2190000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Đen Xám (Graphite)", "MX3S-GRAPHITE", "{\"Màu sắc\":\"Đen Xám (Graphite)\"}", new BigDecimal("2190000"), new BigDecimal("35")));
            vars.add(createVariant(p, "Trắng Xám (Pale Grey)", "MX3S-PALEGREY", "{\"Màu sắc\":\"Trắng Xám (Pale Grey)\"}", new BigDecimal("2190000"), new BigDecimal("25")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 13. Balo laptop chống sốc UniStyle
        productRepository.findBySlug("balo-laptop-chong-soc-unistyle").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Màu sắc\",\"options\":[\"Đen Classic\",\"Xám Tro\",\"Xanh Navy\"]},{\"name\":\"Kích cỡ\",\"options\":[\"Bản Tiêu chuẩn 14 inch\",\"Bản Mở rộng 15.6 - 16 inch\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("389000"));
            List<ProductVariant> vars = new ArrayList<>();
            for (String color : new String[]{"Đen Classic", "Xám Tro", "Xanh Navy"}) {
                for (String size : new String[]{"Bản Tiêu chuẩn 14 inch", "Bản Mở rộng 15.6 - 16 inch"}) {
                    BigDecimal pr = size.contains("Tiêu chuẩn") ? new BigDecimal("389000") : new BigDecimal("429000");
                    String sku = "BALO-" + (color.contains("Đen") ? "BLK" : color.contains("Xám") ? "GRY" : "NVY") + (size.contains("14") ? "-14" : "-16");
                    vars.add(createVariant(p, color + " - " + size, sku, "{\"Màu sắc\":\"" + color + "\",\"Kích cỡ\":\"" + size + "\"}", pr, new BigDecimal("25")));
                }
            }
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        // 14. Thùng 24 lon Nước ngọt Coca-Cola
        productRepository.findBySlug("thung-24-lon-coca-cola-320ml").ifPresent(p -> {
            p.setTierVariation("[{\"name\":\"Quy cách đóng gói\",\"options\":[\"Lon lẻ 320ml\",\"Lốc 6 lon\",\"Thùng 24 lon\"]}]");
            p.setHasVariants(true);
            p.setSellingPrice(new BigDecimal("10000"));
            List<ProductVariant> vars = new ArrayList<>();
            vars.add(createVariant(p, "Lon lẻ 320ml", "COCA-LON-320", "{\"Quy cách đóng gói\":\"Lon lẻ 320ml\"}", new BigDecimal("10000"), new BigDecimal("500")));
            vars.add(createVariant(p, "Lốc 6 lon", "COCA-LOC-6", "{\"Quy cách đóng gói\":\"Lốc 6 lon\"}", new BigDecimal("56000"), new BigDecimal("200")));
            vars.add(createVariant(p, "Thùng 24 lon", "COCA-THUNG-24", "{\"Quy cách đóng gói\":\"Thùng 24 lon\"}", new BigDecimal("195000"), new BigDecimal("150")));
            p.getVariants().clear();
            p.getVariants().addAll(vars);
            productRepository.save(p);
        });

        log.info("Cập nhật dữ liệu ma trận phân loại hàng hoàn tất thành công!");
    }

    private ProductVariant createVariant(Product p, String name, String sku, String attrs, BigDecimal price, BigDecimal stock) {
        return ProductVariant.builder()
                .product(p)
                .variantName(name)
                .sku(sku)
                .attributes(attrs)
                .price(price)
                .stockQuantity(stock)
                .build();
    }
}

