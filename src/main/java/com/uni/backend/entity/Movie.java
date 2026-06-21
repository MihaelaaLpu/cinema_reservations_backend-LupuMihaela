package com.uni.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "movie")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String studio; // Disney, Pixar etc.

    @Min(value = 1)
    @Max(value = 10)
    private Double rating;

    @NotNull
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration; // in minutes

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Screening> screenings = new HashSet<>();
}
