package com.ukayukay.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "contact_no", length = 15)
    private String contactNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;

    @Column(name = "is_discount_elig")
    private Boolean isDiscountElig = false;

    @Column(name = "discount_type", length = 20)
    private String discountType = "NONE";

    @Column(name = "business_name", length = 100)
    private String businessName;

    @Column(name = "business_description", columnDefinition = "TEXT")
    private String businessDescription;

    @Column(name = "seller_approved")
    private Boolean sellerApproved = false;

    @Column(name = "id_number", length = 50)
    private String idNumber; // For storing Senior Citizen ID or PWD ID number


    public enum VerificationStatus {
        PENDING, APPROVED, REJECTED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "business_permit_url")
    private String businessPermitUrl; // Stores image path/link of uploaded document

    @Column(name = "is_active")
    private Boolean isActive = true; 

    @Column(name = "is_shadow_banned")
    private Boolean isShadowBanned = false;

    public enum UserRole {
        buyer, admin, seller
    }
}