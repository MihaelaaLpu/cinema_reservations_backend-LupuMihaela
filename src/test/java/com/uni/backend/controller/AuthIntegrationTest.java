package com.uni.backend.controller;

import com.uni.backend.entity.Role;
import com.uni.backend.entity.User;
import com.uni.backend.repository.RoleRepository;
import com.uni.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        // force Spring to ignore mysql connection completely:
        "spring.datasource.url=jdbc:h2:mem:cinema_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc // http to controller
@ActiveProfiles("test") // read configuration from 'application-test.yml' -> h2 database
@Transactional // after each test, delete the data
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setup() {
        // check USER role to be in the h2 database
        if (roleRepository.findByName("USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);
        }
    }

    @Test
    void endToEnd_RegistrationFlow() throws Exception {
        // the user sens an invalid form with missing password
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        // omit the password
                        .param("userDetails.firstName", "New")
                        .param("userDetails.lastName", "User"))
                .andExpect(status().isOk()) // controller stays on the same page
                .andExpect(view().name("users/auth/register"))
                .andExpect(model().attributeHasFieldErrors("user", "password"));

        // user sends a valid form
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("password", "SecurePass123!")
                        .param("userDetails.firstName", "New")
                        .param("userDetails.lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        // assert
        User savedUser = userRepository.findByUsername("newuser").orElse(null);

        assertNotNull(savedUser, "The user should exist in the database");
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("newuser@example.com", savedUser.getEmail());
    }
}
