package com.ukayukay.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Integer productId;
    private String itemName;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
    private String description;
    private String imagePath;
    private String size;
    private String itemCondition;
    private String status;
    private LocalDate shipmentDate;
    private LocalDate dateAdded;
    private String style;
    private Integer sellerId;
    private String sellerName;
}
