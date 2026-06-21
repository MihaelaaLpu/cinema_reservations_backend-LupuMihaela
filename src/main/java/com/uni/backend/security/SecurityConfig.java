package com.uni.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // csrf active
                // protect endpoints based on roles
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                        // only the ADMIN can create, edit, delete movies or cinema rooms
                        .requestMatchers("/movies/new", "/movies/edit/**", "/movies/delete/**").hasRole("ADMIN")
                        .requestMatchers("/cinema-rooms/new", "/cinema-rooms/edit/**", "/cinema-rooms/delete/**").hasRole("ADMIN")
                        .requestMatchers("/screenings/new", "/screenings/edit/**", "/screenings/delete/**").hasRole("ADMIN")
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        // any user can see the list and buy tickets
                        .anyRequest().authenticated()
                )
                // login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/movies", true)
                        .permitAll()
                )
                // logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // remember me functionality
                .rememberMe(remember -> remember
                        .key("superSecretKey") // unique key
                        .userDetailsService(userDetailsService)
                        .tokenValiditySeconds(86400) // 1 day
                );

        return http.build();
    }
}
