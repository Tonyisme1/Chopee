package com.chopee.modules.auth;

import com.chopee.entity.Shop;
import com.chopee.entity.User;
import com.chopee.entity.enums.Role;
import com.chopee.entity.enums.ShopStatus;
import com.chopee.entity.enums.ShopType;
import com.chopee.entity.enums.UserStatus;
import com.chopee.modules.auth.dto.*;
import com.chopee.repository.ShopRepository;
import com.chopee.repository.UserRepository;
import com.chopee.security.JwtTokenProvider;
import com.chopee.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đăng nhập đã tồn tại trên hệ thống");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã được sử dụng bởi tài khoản khác");
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(user);
        String token = tokenProvider.generateTokenFromUserPrincipal(principal);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(UserProfileResponse.fromEntity(user))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin tài khoản"));

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(UserProfileResponse.fromEntity(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional
    public AuthResponse registerSeller(UserPrincipal principal, RegisterSellerRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        if (user.getShop() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bạn đã đăng ký mở gian hàng rồi");
        }

        String baseSlug = toSlug(request.getShopName());
        String slug = baseSlug;
        int counter = 1;
        while (shopRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + counter++;
        }

        Shop shop = Shop.builder()
                .name(request.getShopName().trim())
                .slug(slug)
                .description(request.getShopDescription())
                .address(request.getShopAddress().trim())
                .phone(request.getShopPhone().trim())
                .logoUrl(request.getShopLogoUrl())
                .bannerUrl(request.getShopBannerUrl())
                .shopType(request.getShopType() != null ? request.getShopType() : ShopType.GENERAL)
                .status(ShopStatus.APPROVED)
                .user(user)
                .build();

        user.setRole(Role.ROLE_SELLER);
        user.setShop(shop);

        shopRepository.save(shop);
        user = userRepository.save(user);

        UserPrincipal updatedPrincipal = UserPrincipal.create(user);
        String newToken = tokenProvider.generateTokenFromUserPrincipal(updatedPrincipal);

        return AuthResponse.builder()
                .accessToken(newToken)
                .tokenType("Bearer")
                .user(UserProfileResponse.fromEntity(user))
                .build();
    }

    private String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "shop-" + System.currentTimeMillis();
        }
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
