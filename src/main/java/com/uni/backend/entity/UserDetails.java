package com.uni.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_details")
public class UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    // optional
    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    // optional
    private String address;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
