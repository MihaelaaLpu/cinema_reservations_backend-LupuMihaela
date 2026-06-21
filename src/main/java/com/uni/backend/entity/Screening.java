package com.uni.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "screening")
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FutureOrPresent(message = "Screening time must be in the future")
    private LocalDateTime startTime;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = true)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "cinema_room_id", nullable = false)
    private CinemaRoom cinemaRoom;

    @OneToMany(mappedBy = "screening", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ticket> tickets = new HashSet<>();

    @Transient
    public LocalDateTime getEndTime() {
        if (this.startTime != null && this.movie != null && this.movie.getDuration() != null) {
            return this.startTime.plusMinutes(this.movie.getDuration());
        }
        return null;
    }
}
