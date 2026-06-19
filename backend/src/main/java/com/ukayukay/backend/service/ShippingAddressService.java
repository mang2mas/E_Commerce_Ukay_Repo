package com.ukayukay.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.ShippingAddress;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.ShippingAddressRepository;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final UserRepository userRepository;

    public ShippingAddressService(ShippingAddressRepository shippingAddressRepository, UserRepository userRepository) {
        this.shippingAddressRepository = shippingAddressRepository;
        this.userRepository = userRepository;
    }

    public ShippingAddress addAddress(Integer userId, ShippingAddress address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(String.format(AppConstants.USER_NOT_FOUND, userId)));

        address.setUser(user);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        return shippingAddressRepository.save(address);
    }

    public List<ShippingAddress> getAddresses(Integer userId) {
        return shippingAddressRepository.findByUser_UserId(userId);
    }

    public ShippingAddress updateAddress(Integer addressId, ShippingAddress updatedAddress) {
        ShippingAddress address = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Shipping address not found with ID: " + addressId));

        if (updatedAddress.getRecipientName() != null) {
            address.setRecipientName(updatedAddress.getRecipientName());
        }
        if (updatedAddress.getPhoneNumber() != null) {
            address.setPhoneNumber(updatedAddress.getPhoneNumber());
        }
        if (updatedAddress.getStreetAddress() != null) {
            address.setStreetAddress(updatedAddress.getStreetAddress());
        }
        if (updatedAddress.getCity() != null) {
            address.setCity(updatedAddress.getCity());
        }
        if (updatedAddress.getProvince() != null) {
            address.setProvince(updatedAddress.getProvince());
        }
        if (updatedAddress.getPostalCode() != null) {
            address.setPostalCode(updatedAddress.getPostalCode());
        }
        if (updatedAddress.getCountry() != null) {
            address.setCountry(updatedAddress.getCountry());
        }
        if (updatedAddress.getIsDefault() != null) {
            address.setIsDefault(updatedAddress.getIsDefault());
        }

        address.setUpdatedAt(LocalDateTime.now());
        return shippingAddressRepository.save(address);
    }

    public void deleteAddress(Integer addressId) {
        if (!shippingAddressRepository.existsById(addressId)) {
            throw new RuntimeException("Shipping address not found with ID: " + addressId);
        }
        shippingAddressRepository.deleteById(addressId);
    }
}
