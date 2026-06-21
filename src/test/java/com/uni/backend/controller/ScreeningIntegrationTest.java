package com.uni.backend.controller;

import com.uni.backend.entity.CinemaRoom;
import com.uni.backend.entity.Movie;
import com.uni.backend.entity.Screening;
import com.uni.backend.repository.CinemaRoomRepository;
import com.uni.backend.repository.MovieRepository;
import com.uni.backend.repository.ScreeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class ScreeningIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    private Long movieId;
    private Long roomId;

    @BeforeEach
    void setup() {
        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setDuration(120);
        movie.setStudio("Warner");
        movie.setRating(9.0);
        movie = movieRepository.save(movie);
        movieId = movie.getId();

        CinemaRoom room = new CinemaRoom();
        room.setRoomCode("A10");
        room.setCapacity(50);
        room.setIs3D(false);
        room = cinemaRoomRepository.save(room);
        roomId = room.getId();

        Screening initialScreening = new Screening();
        initialScreening.setMovie(movie);
        initialScreening.setCinemaRoom(room);
        initialScreening.setStartTime(LocalDateTime.of(2026, 10, 10, 12, 0));
        screeningRepository.save(initialScreening);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void endToEnd_ScreeningOverlapFlow() throws Exception {
        // simulate overlapping
        mockMvc.perform(post("/screenings")
                        .with(csrf())
                        .param("movie.id", movieId.toString())
                        .param("cinemaRoom.id", roomId.toString())
                        .param("startTime", "2026-10-10T13:00"))
                .andExpect(status().isOk()) // Rămâne pe pagină
                .andExpect(view().name("screenings/create"))
                .andExpect(model().attributeExists("overlapError"));

        // simulate correct flow
        mockMvc.perform(post("/screenings")
                        .with(csrf())
                        .param("movie.id", movieId.toString())
                        .param("cinemaRoom.id", roomId.toString())
                        .param("startTime", "2026-10-10T15:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/screenings"));

        // check for 2 screenings in the database
        assertEquals(2, screeningRepository.count());
    }
}
