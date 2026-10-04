package com.chopee.modules.order.dto;

import com.chopee.entity.enums.PaymentMethod;
import com.chopee.entity.enums.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    private String shippingName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    private String shippingPhone;

    @NotBlank(message = "Địa chỉ nhận hàng không được để trống")
    private String shippingAddress;

    @Builder.Default
    private ShippingMethod shippingMethod = ShippingMethod.STANDARD;

    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    private String note;

    private List<Long> cartItemIds; // Nếu null hoặc rỗng -> Đặt hàng toàn bộ giỏ
}
