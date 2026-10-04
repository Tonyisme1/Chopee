package com.chopee.modules.payment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
public class VNPayConfig {
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl = "http://localhost:5173/payment/vnpay-callback";
    private String tmnCode = "CHOPEE01";
    private String hashSecret = "CHOPEEVNPAYSECRETKEY20261004SANDBOX";
    private String version = "2.1.0";
    private String command = "pay";
}

