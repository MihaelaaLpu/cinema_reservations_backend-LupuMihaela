package com.uni.backend.config;

import com.uni.backend.entity.CinemaRoom;
import com.uni.backend.entity.Movie;
import com.uni.backend.entity.Role;
import com.uni.backend.entity.User;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.repository.CinemaRoomRepository;
import com.uni.backend.repository.MovieRepository;
import com.uni.backend.repository.RoleRepository;
import com.uni.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final Random random = new Random();

    private final MovieRepository movieRepository;
    private final CinemaRoomRepository cinemaRoomRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

//        movieRepository.deleteAll();
//        cinemaRoomRepository.deleteAll();
//        userRepository.deleteAll();

        log.info("Check if there is data into the db...");

        // generate movies only if the table is empty
        if (movieRepository.count() == 0) {
            log.info("Generate dummy data for MOVIE table...");
            List<Movie> movies = new ArrayList<>();
            for (int i = 1; i <= 25; i++) {
                Movie m = new Movie();
                m.setTitle("Movie " + i);
                m.setStudio("Studio " + (i % 5));
                m.setDuration(90 + i); // 90 <= duration <= 140
                m.setRating(5.0 + (i % 5)); // 5 <= rating <= 10
                movies.add(m);
            }
            movieRepository.saveAll(movies);
        }

        // generate cinema rooms
        if (cinemaRoomRepository.count() == 0) {
            log.info("Generate dummy data for CINEMA_ROOM table...");
            List<CinemaRoom> rooms = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                CinemaRoom room = new CinemaRoom();

                char randomLetter = (char) ('A' + random.nextInt(26));
                String randomNumber = String.format("%02d", random.nextInt(100));
                String randomRoomCode = randomLetter + randomNumber;

                room.setRoomCode(randomRoomCode); // A27 or A01
                room.setCapacity(30 + (i * 10)); // 40 <= capacity <= 180
                room.setIs3D(i % 3 == 0); // 1 of 3 rooms is 3D
                rooms.add(room);
            }
            cinemaRoomRepository.saveAll(rooms);
        }

        // generate 2 roles
        if (roleRepository.count() == 0) {
            log.info("Generate dummy data for ROLE table...");
            Role userRole = new Role();
            userRole.setName("USER");
            Role adminRole = new Role();
            adminRole.setName("ADMIN");

            roleRepository.saveAll(List.of(userRole, adminRole));
        }

        // generate users
        if (userRepository.count() == 0) {
            log.info("Generate dummy data for USER table...");

            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "role", "USER"));
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "role", "ADMIN"));

            List<User> users = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                User u = new User();
                u.setUsername("test_user_" + i);
                u.setEmail("user" + i + "@cinema.com");
                u.setPassword(passwordEncoder.encode("test1234"));

                if (i % 10 == 0) {
                    u.setRoles(Set.of(adminRole));
                } else {
                    u.setRoles(Set.of(userRole));
                }

                users.add(u);
            }
            userRepository.saveAll(users);
        }

        log.info("Database successfully updated!");
    }
}