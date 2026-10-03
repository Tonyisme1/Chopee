package com.chopee.modules.auth.dto;

import com.chopee.entity.User;
import com.chopee.entity.enums.Role;
import com.chopee.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private Role role;
    private UserStatus status;
    private Long shopId;
    private String shopName;

    public static UserProfileResponse fromEntity(User user) {
        Long shopId = null;
        String shopName = null;

        if (user.getShop() != null) {
            shopId = user.getShop().getId();
            shopName = user.getShop().getName();
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .shopId(shopId)
                .shopName(shopName)
                .build();
    }
}
