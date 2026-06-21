package com.uni.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Choose for whom is the ticket")
    private String ticketType; // CHILD, ADULT, SENIOR

    @Min(value = 0)
    private Float price;

    @NotBlank(message = "Seat row is required")
    private String seatRow; // A, B, C etc.

    @Min(value = 1)
    private Integer seatNumber; // 12, 13, 14 etc.

    private LocalDateTime purchaseDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;
}
