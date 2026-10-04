package com.chopee.modules.admin;

import com.chopee.common.dto.PageResponse;
import com.chopee.entity.Order;
import com.chopee.entity.Shop;
import com.chopee.entity.enums.OrderStatus;
import com.chopee.entity.enums.Role;
import com.chopee.entity.enums.ShopStatus;
import com.chopee.modules.admin.dto.AdminDashboardResponse;
import com.chopee.modules.admin.dto.UpdateShopStatusRequest;
import com.chopee.modules.catalog.ProductService;
import com.chopee.modules.catalog.dto.ShopPublicResponse;
import com.chopee.repository.OrderRepository;
import com.chopee.repository.ShopRepository;
import com.chopee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalSellers = userRepository.countByRole(Role.ROLE_SELLER);
        long totalBuyers = userRepository.countByRole(Role.ROLE_BUYER);

        long totalShops = shopRepository.count();
        long pendingShops = shopRepository.countByStatus(ShopStatus.PENDING);
        long approvedShops = shopRepository.countByStatus(ShopStatus.APPROVED);
        long lockedShops = shopRepository.countByStatus(ShopStatus.LOCKED);

        long totalOrders = orderRepository.count();
        BigDecimal totalGmv = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalSellers(totalSellers)
                .totalBuyers(totalBuyers)
                .totalShops(totalShops)
                .pendingShops(pendingShops)
                .approvedShops(approvedShops)
                .lockedShops(lockedShops)
                .totalOrders(totalOrders)
                .totalGmv(totalGmv)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ShopPublicResponse> getShops(ShopStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), (size <= 0 || size > 100) ? 20 : size);
        Page<Shop> shopPage = (status != null)
                ? shopRepository.findByStatus(status, pageable)
                : shopRepository.findAll(pageable);

        List<ShopPublicResponse> content = shopPage.getContent().stream()
                .map(productService::mapToShopResponse)
                .collect(Collectors.toList());

        return PageResponse.from(shopPage, content);
    }

    @Transactional
    public ShopPublicResponse updateShopStatus(Long shopId, UpdateShopStatusRequest request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy gian hàng"));

        shop.setStatus(request.getStatus());
        Shop saved = shopRepository.save(shop);
        return productService.mapToShopResponse(saved);
    }
}
