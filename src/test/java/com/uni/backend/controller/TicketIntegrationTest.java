package com.uni.backend.controller;

import com.uni.backend.entity.CinemaRoom;
import com.uni.backend.entity.Movie;
import com.uni.backend.entity.Screening;
import com.uni.backend.entity.User;
import com.uni.backend.repository.*;
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
public class TicketIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private Long screeningId;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setUsername("student1");
        user.setEmail("student1@example.com");
        user.setPassword("pass");
        userRepository.save(user);

        Movie movie = new Movie();
        movie.setTitle("Avatar");
        movie.setDuration(160);
        movie.setRating(8.0);
        movieRepository.save(movie);

        CinemaRoom room = new CinemaRoom();
        room.setRoomCode("B02");
        room.setCapacity(100);
        room.setIs3D(true);
        cinemaRoomRepository.save(room);

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setCinemaRoom(room);
        screening.setStartTime(LocalDateTime.now().plusDays(1));
        screening = screeningRepository.save(screening);
        screeningId = screening.getId();
    }

    @Test
    @WithMockUser(username = "student1", roles = {"USER"})
    void endToEnd_TicketBuyingAndListingFlow() throws Exception {
        // a user buys a ticket for a CHILD
        mockMvc.perform(post("/tickets")
                        .with(csrf())
                        .param("ticketType", "CHILD")
                        .param("seatRow", "C")
                        .param("seatNumber", "15")
                        .param("screening.id", screeningId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets"));

        // check price: 50% discount from 25.0 = 12.5
        assertEquals(1, ticketRepository.count());
        assertEquals(12.5f, ticketRepository.findAll().get(0).getPrice());

        // the user checks its list of tickets
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/list"))
                .andExpect(model().attributeExists("tickets"))
                .andExpect(model().attribute("totalItems", 1L)); // expects exactly one ticket
    }
}
