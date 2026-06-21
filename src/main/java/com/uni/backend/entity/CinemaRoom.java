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
@Table(name = "cinema_room")
public class CinemaRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room code is required")
    private String roomCode; // A27 or A01

    @NotNull(message = "Capacity is required")
    @Min(value = 10, message = "A room must have at least 10 seats")
    private Integer capacity;

    @NotNull(message = "Please specify if the room is 3D or not")
    private Boolean is3D;

    @OneToMany(mappedBy = "cinemaRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Screening> screenings = new HashSet<>();
}
