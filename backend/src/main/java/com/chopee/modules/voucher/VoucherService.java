package com.chopee.modules.voucher;

import com.chopee.entity.Shop;
import com.chopee.entity.Voucher;
import com.chopee.entity.enums.DiscountType;
import com.chopee.modules.voucher.dto.CreateVoucherRequest;
import com.chopee.modules.voucher.dto.ValidateVoucherResponse;
import com.chopee.modules.voucher.dto.VoucherResponse;
import com.chopee.repository.ShopRepository;
import com.chopee.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final ShopRepository shopRepository;

    @Transactional(readOnly = true)
    public List<VoucherResponse> getPlatformVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findActivePlatformVouchers(now).stream()
                .map(VoucherResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getShopVouchers(Long shopId) {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findActiveShopVouchers(shopId, now).stream()
                .map(VoucherResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getSellerVouchers(Long sellerUserId) {
        Shop shop = shopRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chưa có gian hàng người bán"));
        return voucherRepository.findByShopIdAndIsDeletedFalse(shop.getId()).stream()
                .map(VoucherResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public VoucherResponse createShopVoucher(Long sellerUserId, CreateVoucherRequest request) {
        Shop shop = shopRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chưa có gian hàng người bán"));

        String cleanCode = request.getCode().trim().toUpperCase();
        if (voucherRepository.existsByCode(cleanCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã voucher '" + cleanCode + "' đã tồn tại trên hệ thống");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        Voucher voucher = Voucher.builder()
                .code(cleanCode)
                .shop(shop)
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit() != null ? request.getUsageLimit() : 1000)
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isDeleted(false)
                .build();

        Voucher saved = voucherRepository.save(voucher);
        return VoucherResponse.fromEntity(saved);
    }

    @Transactional
    public VoucherResponse createPlatformVoucher(CreateVoucherRequest request) {
        String cleanCode = request.getCode().trim().toUpperCase();
        if (voucherRepository.existsByCode(cleanCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã voucher '" + cleanCode + "' đã tồn tại trên hệ thống");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        Voucher voucher = Voucher.builder()
                .code(cleanCode)
                .shop(null) // Voucher toàn sàn
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit() != null ? request.getUsageLimit() : 5000)
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isDeleted(false)
                .build();

        Voucher saved = voucherRepository.save(voucher);
        return VoucherResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ValidateVoucherResponse validateVoucher(String code, Long shopId, BigDecimal orderAmount) {
        if (code == null || code.isBlank()) {
            return ValidateVoucherResponse.builder()
                    .code(code)
                    .isValid(false)
                    .message("Mã voucher không được để trống")
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .build();
        }

        String cleanCode = code.trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        Voucher voucher = voucherRepository.findValidVoucher(cleanCode, now).orElse(null);

        if (voucher == null) {
            return ValidateVoucherResponse.builder()
                    .code(cleanCode)
                    .isValid(false)
                    .message("Mã voucher không tồn tại, đã hết hạn hoặc hết lượt sử dụng")
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .build();
        }

        if (voucher.getShop() != null && (shopId == null || !voucher.getShop().getId().equals(shopId))) {
            return ValidateVoucherResponse.builder()
                    .code(cleanCode)
                    .isValid(false)
                    .message("Mã voucher này chỉ áp dụng riêng cho gian hàng " + voucher.getShop().getName())
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .build();
        }

        BigDecimal safeOrderAmount = orderAmount != null ? orderAmount : BigDecimal.ZERO;
        if (safeOrderAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            return ValidateVoucherResponse.builder()
                    .code(cleanCode)
                    .isValid(false)
                    .message(String.format("Đơn hàng chưa đạt giá trị tối thiểu %,.0f VNĐ", voucher.getMinOrderAmount()))
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(safeOrderAmount)
                    .build();
        }

        BigDecimal discount;
        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            discount = safeOrderAmount.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscountAmount() != null && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discount = voucher.getMaxDiscountAmount();
            }
        } else {
            discount = voucher.getDiscountValue();
        }

        if (discount.compareTo(safeOrderAmount) > 0) {
            discount = safeOrderAmount;
        }

        BigDecimal finalAmount = safeOrderAmount.subtract(discount);

        return ValidateVoucherResponse.builder()
                .code(cleanCode)
                .isValid(true)
                .message("Áp dụng mã giảm giá thành công")
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .build();
    }
}
