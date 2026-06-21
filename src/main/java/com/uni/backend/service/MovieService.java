package com.uni.backend.service;

import com.uni.backend.entity.Movie;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MovieService {
    // create
    Movie createMovie(Movie movie);

    // read
    List<Movie> getAllMovies();
    Movie getMovieById(Long id);

    // update
    Movie updateMovie(Movie movie, Long id);

    // delete
    void deleteMovie(Long id);

    Page<Movie> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection);
}