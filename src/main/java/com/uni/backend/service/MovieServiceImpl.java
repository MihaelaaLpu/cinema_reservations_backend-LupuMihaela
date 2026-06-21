package com.uni.backend.service;

import com.uni.backend.entity.Movie;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.repository.MovieRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public Movie createMovie(Movie movie) {
        log.info("Creating movie {}...", movie.getTitle());
        return movieRepository.save(movie);
    }

    @Override
    public List<Movie> getAllMovies() {
        log.info("Retrieving all movies from database...");
        return movieRepository.findAll();
    }

    @Override
    public Movie getMovieById(Long id) {
        log.debug("Retrieving movie with ID {}...", id);
        return movieRepository.findById(id).orElseThrow(() -> {
            log.error("Movie with ID {} was not found in the database", id);
            return new ResourceNotFoundException("Movie", "id", id);
        });
    }

    @Override
    @Transactional
    public Movie updateMovie(Movie movie, Long id) {
        log.debug("Updating movie {}...", movie.getTitle());

        log.debug("Retrieving movie with ID {}...", id);
        Movie existingMovie = movieRepository.findById(id).orElseThrow(() -> {
            log.error("Movie with ID {} was not found in the database", id);
            return new ResourceNotFoundException("Movie", "id", id);
        });

        log.info("Update the fields for movie {}...", movie.getTitle());
        existingMovie.setTitle(movie.getTitle());
        existingMovie.setStudio(movie.getStudio());
        existingMovie.setRating(movie.getRating());
        existingMovie.setDuration(movie.getDuration());

        log.info("Save and returned the updated movie...");
        return movieRepository.save(existingMovie);
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        log.debug("Deleting movie {}...", id);

        log.info("Check if the movie exists else throw an error...");
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        movieRepository.delete(existingMovie);
    }

    @Override
    public Page<Movie> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        log.info("Asc/ Desc sorting...");
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        log.info("Create Pageable...");
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

        log.info("Return the page...");
        return movieRepository.findAll(pageable);
    }
}
