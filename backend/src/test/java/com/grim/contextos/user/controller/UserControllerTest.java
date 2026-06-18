package com.grim.contextos.user.controller;

import com.grim.contextos.auth.model.Role;
import com.grim.contextos.auth.model.UserPrincipal;
import com.grim.contextos.user.model.User;
import com.grim.contextos.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=test-secret-key-that-is-long-enough-for-hmac-sha-512-algorithm"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedUser;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        // Create a real user in the database
        User user = new User("profile@test.com", passwordEncoder.encode("Password123!"), "Profile User");
        savedUser = userRepository.save(user);
        principal = new UserPrincipal(savedUser.getId(), savedUser.getEmail(), savedUser.getPasswordHash(), Role.USER);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void getMeReturnsProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(savedUser.getId().toString()))
            .andExpect(jsonPath("$.data.email").value("profile@test.com"))
            .andExpect(jsonPath("$.data.displayName").value("Profile User"))
            .andExpect(jsonPath("$.data.avatarUrl").value(""))
            .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void getMeReturnsEmptyAvatarWhenNull() throws Exception {
        savedUser.setAvatarUrl(null);
        userRepository.save(savedUser);

        mockMvc.perform(get("/api/v1/users/me")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.avatarUrl").value(""));
    }

    @Test
    void getMeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateProfileUpdatesDisplayName() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"displayName":"New Name"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.displayName").value("New Name"))
            .andExpect(jsonPath("$.data.email").value("profile@test.com"));
    }

    @Test
    void updateProfileUpdatesAvatarUrl() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"avatarUrl":"https://example.com/avatar.png"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.avatarUrl").value("https://example.com/avatar.png"));
    }

    @Test
    void updateProfilePartialUpdatePreservesOtherFields() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"displayName":"Only Name"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.displayName").value("Only Name"))
            .andExpect(jsonPath("$.data.email").value("profile@test.com"));
    }

    @Test
    void updateProfileRequiresAuthentication() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"test\"}"))
            .andExpect(status().isForbidden());
    }
}
