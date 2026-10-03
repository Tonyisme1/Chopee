package com.chopee.entity.enums;

public enum OrderStatus {
    PENDING,        // Chờ người bán xác nhận
    CONFIRMED,      // Shop đang chuẩn bị hàng
    SHIPPING,       // Đang giao hàng
    DELIVERED,      // Đã giao thành công
    CANCELLED       // Đã hủy đơn
}

