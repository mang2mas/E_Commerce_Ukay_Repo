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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.service.ProductService;
import com.ukayukay.backend.util.AppConstants;


@RestController
@RequestMapping("/api/products")
public class ProductController {


    private final ProductService productService;


    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping
    public List<Product> getAllProducts(@RequestParam(required = false) String category) {
        if (category != null && !category.trim().isEmpty()) {
            return productService.getAvailableProductsByCategory(category);
        }
        return productService.getAvailableProducts();
    }


    @GetMapping("/admin")
    public List<Product> getAllProductsForAdmin() {
        return productService.getAllProducts();
    }


    @GetMapping("/recommended")
    public List<Product> getRecommendedProducts(@RequestParam(required = false) Integer userId,
                                                @RequestParam(required = false, defaultValue = "30") int limit) {
        // Default limit set to 30 for FYP/home feed
        return productService.getRecommendedProducts(userId, limit);
    }


    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Product>> getProductsBySeller(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }


    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }


    @PostMapping("/seller/{sellerId}")
    public Product addProductAsSeller(@PathVariable Integer sellerId, @RequestBody Product product) {
        return productService.createProductForSeller(sellerId, product);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(String.format(AppConstants.PRODUCT_DELETED, id));
    }
}





