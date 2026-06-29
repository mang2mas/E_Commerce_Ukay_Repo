package com.ukayukay.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ukayukay.backend.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequest {

    @NotBlank(message = "Email or username is required")
    @Size(max = 100, message = "Email or username must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Pattern(regexp = "^09\\d{9}$", message = "Contact number must be 11 digits and start with 09")
    private String contactNo;
    private Boolean isPwdSenior;
    private String idNumber;

    private String accountType;

    private User.UserRole role;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public Boolean getIsPwdSenior() { return isPwdSenior; }
    public void setIsPwdSenior(Boolean isPwdSenior) { this.isPwdSenior = isPwdSenior; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public User.UserRole getRole() { return role; }
    public void setRole(User.UserRole role) { this.role = role; }
}
