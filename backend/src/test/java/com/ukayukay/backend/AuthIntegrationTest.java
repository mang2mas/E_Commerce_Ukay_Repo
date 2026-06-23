package com.ukayukay.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukayukay.backend.dto.AuthRequest;
import com.ukayukay.backend.dto.RegisterRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void registerUser_validInput_returns200WithUser() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("buyer@test.com");
        req.setPassword("password123");
        req.setFullName("Test Buyer");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("buyer@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist()); // password must not be returned
    }

    @Test
    @Order(2)
    void registerUser_duplicateEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("buyer@test.com");
        req.setPassword("password123");
        req.setFullName("Duplicate Buyer");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(3)
    void registerUser_missingEmail_returns422() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setPassword("password123");
        req.setFullName("No Email User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @Order(4)
    void login_validCredentials_returnsJwtToken() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("buyer@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email").value("buyer@test.com"))
                .andExpect(jsonPath("$.role").value("buyer"))
                .andReturn();
    }

    @Test
    @Order(5)
    void login_wrongPassword_returns400() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("buyer@test.com");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(6)
    void login_unknownEmail_returns400() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("nobody@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
