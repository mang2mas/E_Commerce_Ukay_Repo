package com.ukayukay.backend.dto;

import com.ukayukay.backend.entity.User.UserRole;
import com.ukayukay.backend.entity.User.VerificationStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Integer userId;
    private String email;
    private String fullName;
    private String contactNo;
    private UserRole role;
    private Boolean isDiscountElig;
    private String discountType;
    private String businessName;
    private String businessDescription;
    private Boolean sellerApproved;
    private String idNumber;
    private Boolean isPwdSenior;
    private VerificationStatus verificationStatus;
    private String businessPermitUrl;
    private Boolean isActive;
    private Boolean isShadowBanned;
}
