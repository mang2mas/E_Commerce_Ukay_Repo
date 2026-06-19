package com.ukayukay.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "*")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    public ShippingAddressController(ShippingAddressService shippingAddressService) {
        this.shippingAddressService = shippingAddressService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> addAddress(@PathVariable Integer userId, @RequestBody ShippingAddress address) {
        try {
            return ResponseEntity.ok(shippingAddressService.addAddress(userId, address));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ShippingAddress>> getAddresses(@PathVariable Integer userId) {
        return ResponseEntity.ok(shippingAddressService.getAddresses(userId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable Integer addressId, @RequestBody ShippingAddress updatedAddress) {
        try {
            return ResponseEntity.ok(shippingAddressService.updateAddress(addressId, updatedAddress));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer addressId) {
        try {
            shippingAddressService.deleteAddress(addressId);
            return ResponseEntity.ok("Address deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
