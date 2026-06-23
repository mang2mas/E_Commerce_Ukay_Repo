package com.ukayukay.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.UserRepository;
import com.ukayukay.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String buyerToken;
    private Integer buyerId;

    @BeforeEach
    void setUp() {
        User buyer = new User();
        buyer.setEmail("ordertestbuyer@test.com");
        buyer.setPassword(passwordEncoder.encode("password123"));
        buyer.setFullName("Order Test Buyer");
        buyer.setRole(User.UserRole.buyer);
        buyer.setIsActive(true);
        User saved = userRepository.save(buyer);
        buyerId = saved.getUserId();
        buyerToken = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name(), saved.getUserId());
    }

    @Test
    void checkout_emptyCart_returns400() throws Exception {
        Map<String, String> request = Map.of(
                "shippingAddress", "123 Test Street",
                "paymentMethod", "CASH_ON_DELIVERY"
        );

        mockMvc.perform(post("/api/orders/checkout/" + buyerId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Your shopping cart is empty!"));
    }

    @Test
    void checkout_missingShippingAddress_returns400() throws Exception {
        Map<String, String> request = Map.of("paymentMethod", "CASH_ON_DELIVERY");

        mockMvc.perform(post("/api/orders/checkout/" + buyerId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getCustomerOrders_authenticated_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/orders/customer/" + buyerId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getCustomerOrders_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/orders/customer/" + buyerId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_withBuyerToken_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/requests")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicProductListing_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }
}
