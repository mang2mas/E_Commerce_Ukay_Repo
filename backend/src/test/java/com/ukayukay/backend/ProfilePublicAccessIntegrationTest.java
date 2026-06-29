package com.ukayukay.backend;

import com.ukayukay.backend.dto.RegisterRequest;
import com.ukayukay.backend.entity.User;
import com.ukayukay.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfilePublicAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void profileEndpoint_isAccessibleWithoutAuthentication() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("public-profile@test.com");
        req.setPassword("password123");
        req.setFullName("Public Profile User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"public-profile@test.com\",\"password\":\"password123\",\"fullName\":\"Public Profile User\"}"))
                .andExpect(status().isOk());

        User createdUser = userRepository.findByEmail("public-profile@test.com")
                .orElseThrow(() -> new AssertionError("User should be created"));

        mockMvc.perform(get("/api/profile/{userId}", createdUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("public-profile@test.com"));
    }
}
