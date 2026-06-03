package com.ukayukay.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 1;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_path")
    private String imagePath;
}