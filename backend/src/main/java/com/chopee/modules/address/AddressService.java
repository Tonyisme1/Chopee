package com.chopee.modules.address;

import com.chopee.entity.User;
import com.chopee.entity.UserAddress;
import com.chopee.modules.address.dto.AddressRequest;
import com.chopee.modules.address.dto.AddressResponse;
import com.chopee.repository.UserAddressRepository;
import com.chopee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDesc(userId).stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin người dùng"));

        List<UserAddress> existingAddresses = userAddressRepository.findByUserId(userId);
        boolean shouldBeDefault = existingAddresses.isEmpty() || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault && !existingAddresses.isEmpty()) {
            for (UserAddress addr : existingAddresses) {
                if (Boolean.TRUE.equals(addr.getIsDefault())) {
                    addr.setIsDefault(false);
                    userAddressRepository.save(addr);
                }
            }
        }

        UserAddress address = UserAddress.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .detailAddress(request.getDetailAddress())
                .isDefault(shouldBeDefault)
                .build();

        UserAddress saved = userAddressRepository.save(address);
        return AddressResponse.fromEntity(saved);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        UserAddress target = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Địa chỉ không tồn tại hoặc không thuộc quyền sở hữu của bạn"));

        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
        for (UserAddress addr : addresses) {
            addr.setIsDefault(addr.getId().equals(addressId));
            userAddressRepository.save(addr);
        }

        target.setIsDefault(true);
        return AddressResponse.fromEntity(target);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress target = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Địa chỉ không tồn tại hoặc không thuộc quyền sở hữu của bạn"));

        boolean wasDefault = Boolean.TRUE.equals(target.getIsDefault());
        userAddressRepository.delete(target);

        if (wasDefault) {
            List<UserAddress> remaining = userAddressRepository.findByUserId(userId);
            if (!remaining.isEmpty()) {
                UserAddress newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                userAddressRepository.save(newDefault);
            }
        }
    }
}
