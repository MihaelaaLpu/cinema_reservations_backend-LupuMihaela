package com.uni.backend.repository;

import com.uni.backend.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTitle(String title);
    Optional<List<Movie>> findMovieByStudio(String studio);
    void deleteAll();
}
