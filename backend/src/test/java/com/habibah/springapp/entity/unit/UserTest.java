package com.habibah.springapp.entity.unit;

import com.habibah.springapp.entity.User;
import com.habibah.springapp.entity.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserTest {
    @Test
    void testShouldCreateUserWithBuilder() {
        User user = User.builder()
                .id(1L)
                .email("janedoe@example.com")
                .fullName("Jane Doe")
                .password("password")
                .role(Role.USER)
                .build();

        assertEquals(1L, user.getId());
        assertEquals("janedoe@example.com", user.getEmail());
        assertEquals("Jane Doe", user.getFullName());
        assertEquals("password", user.getPassword());
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void testShouldCreateUserWithAllArgsConstructor() {
        User user = new User(
                1L,
                "janedoe@example.com",
                "Jane Doe",
                "password",
                Role.ADMIN
        );

        assertEquals(1L, user.getId());
        assertEquals("janedoe@example.com", user.getEmail());
        assertEquals("Jane Doe", user.getFullName());
        assertEquals("password", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void testShouldCreateEmptyUserWithNoArgsConstructor() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getRole());
        assertNull(user.getPassword());
        assertNull(user.getFullName());
        assertNull(user.getEmail());
    }

    @Test
    void testShouldUpdateUserFieldsWithSetters() {
        User user = User.builder()
                .id(1L)
                .email("janedoe@example.com")
                .fullName("Jane Doe")
                .password("password")
                .role(Role.USER)
                .build();

        user.setEmail("new@example.com");
        user.setFullName("Jane Smith");
        user.setPassword("newpassword");
        user.setRole(Role.ADMIN);

        assertEquals("new@example.com", user.getEmail());
        assertEquals("Jane Smith", user.getFullName());
        assertEquals("newpassword", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
    }
}
