package com.uni.backend.controller;

import com.uni.backend.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class MovieIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @BeforeEach
    void setup() {
        movieRepository.deleteAll();
    }

    // an unauthenticated user accessing the movies list should be directed to log in
    @Test
    void shouldRedirectToLogin_WhenUnauthenticatedUserTriesToAccessMovies() throws Exception {
        mockMvc.perform(get("/movies"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // an authenticated user can access the movies list should be directed to log in
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldReturnMoviesList_WhenUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/list"))
                .andExpect(model().attributeExists("movies"));
    }

    // only an admin can create a movie
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateMovieInDatabase_WhenAdminPostsData() throws Exception {
        mockMvc.perform(post("/movies")
                        .with(csrf())
                        .param("title", "The Matrix")
                        .param("studio", "Warner Bros")
                        .param("duration", "136")
                        .param("rating", "8.7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies"));

        assertEquals(1, movieRepository.count());
    }
}
