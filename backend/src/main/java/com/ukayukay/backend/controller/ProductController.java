package com.ukayukay.backend.controller;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukayukay.backend.entity.Product;
import com.ukayukay.backend.service.ProductService;
import com.ukayukay.backend.util.AppConstants;


@RestController
@RequestMapping("/api/products")
public class ProductController {


    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public ProductController(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
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


    @PostMapping(value = "/seller/{sellerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product addProductAsSellerMultipart(
            @PathVariable Integer sellerId,
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        Product product = objectMapper.readValue(productJson, Product.class);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveProductImage(imageFile);
            product.setImagePath(imagePath);
        }

        return productService.createProductForSeller(sellerId, product);
    }

    @PostMapping(value = "/seller/{sellerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Product addProductAsSellerJson(@PathVariable Integer sellerId, @RequestBody Product product) {
        return productService.createProductForSeller(sellerId, product);
    }

    private String saveProductImage(MultipartFile imageFile) throws IOException {
        Path uploadDir = Paths.get("uploads", "product-images");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String extension = "jpg";
        String originalName = imageFile.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }

        String imageName = UUID.randomUUID().toString() + "." + extension;
        Path target = uploadDir.resolve(imageName);

        try (InputStream inputStream = imageFile.getInputStream()) {
            Files.copy(inputStream, target);
        }

        return "/images/uploaded/" + imageName;
    }


    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> updateProductImage(
            @PathVariable Integer id,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        if (imageFile == null || imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Product update = new Product();
        update.setImagePath(saveProductImage(imageFile));
        return ResponseEntity.ok(productService.updateProduct(id, update));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(String.format(AppConstants.PRODUCT_DELETED, id));
    }
}





