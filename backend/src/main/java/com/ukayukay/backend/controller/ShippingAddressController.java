package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.ShippingAddress;
import com.ukayukay.backend.service.ShippingAddressService;

@RestController
@RequestMapping("/api/addresses")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    public ShippingAddressController(ShippingAddressService shippingAddressService) {
        this.shippingAddressService = shippingAddressService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ShippingAddress> addAddress(@PathVariable Integer userId, @RequestBody ShippingAddress address) {
        return ResponseEntity.ok(shippingAddressService.addAddress(userId, address));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ShippingAddress>> getAddresses(@PathVariable Integer userId) {
        return ResponseEntity.ok(shippingAddressService.getAddresses(userId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ShippingAddress> updateAddress(@PathVariable Integer addressId, @RequestBody ShippingAddress updatedAddress) {
        return ResponseEntity.ok(shippingAddressService.updateAddress(addressId, updatedAddress));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Integer addressId) {
        shippingAddressService.deleteAddress(addressId);
        return ResponseEntity.ok("Address deleted successfully.");
    }
}

