package com.chopee.modules.payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayHelper {

    /**
     * Mã hóa dữ liệu bằng thuật toán HMAC-SHA512 theo chuẩn của VNPay
     */
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                return null;
            }
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi sinh chữ ký HMAC-SHA512 VNPay", ex);
        }
    }

    /**
     * Tạo chuỗi hash data (query string đã sắp xếp alphabetically theo tên tham số và encode UTF-8 / US-ASCII)
     */
    public static String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if (fieldValue != null && !fieldValue.trim().isEmpty()
                        && !fieldName.equals("vnp_SecureHash")
                        && !fieldName.equals("vnp_SecureHashType")) {
                    hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi định dạng hash data", e);
        }
        return hashData.toString();
    }

    /**
     * Tạo toàn bộ query string bao gồm chữ ký số vnp_SecureHash
     */
    public static String buildQueryUrl(Map<String, String> params, String secretKey) {
        String hashData = buildHashData(params);
        String vnpSecureHash = hmacSHA512(secretKey, hashData);
        return hashData + "&vnp_SecureHash=" + vnpSecureHash;
    }

    /**
     * Xác thực chữ ký số từ IPN hoặc Return Callback của VNPay
     */
    public static boolean verifySignature(Map<String, String> fields, String secretKey) {
        String vnpSecureHash = fields.get("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
            return false;
        }

        Map<String, String> filteredFields = new HashMap<>(fields);
        filteredFields.remove("vnp_SecureHash");
        filteredFields.remove("vnp_SecureHashType");

        String hashData = buildHashData(filteredFields);
        String signValue = hmacSHA512(secretKey, hashData);

        return vnpSecureHash.equalsIgnoreCase(signValue);
    }
}

