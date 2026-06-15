package com.grim.contextos.user.service;

import com.grim.contextos.auth.model.Role;
import com.grim.contextos.user.model.User;
import com.grim.contextos.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private final UUID userId = UUID.randomUUID();
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);

        testUser = new User("test@test.com", "hash", "Test User");
        testUser.setId(userId);
    }

    @Test
    void getUserByIdReturnsUserWhenFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(userId);

        assertEquals(userId, result.getId());
        assertEquals("test@test.com", result.getEmail());
        assertEquals("Test User", result.getDisplayName());
    }

    @Test
    void getUserByIdThrowsWhenNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        var ex = assertThrows(RuntimeException.class, () -> userService.getUserById(userId));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getUserByIdWithDifferentId() {
        UUID otherId = UUID.randomUUID();
        User otherUser = new User("other@test.com", "hash", "Other");
        otherUser.setId(otherId);

        when(userRepository.findById(otherId)).thenReturn(Optional.of(otherUser));

        User result = userService.getUserById(otherId);
        assertEquals(otherId, result.getId());
        assertEquals("other@test.com", result.getEmail());
    }
}
