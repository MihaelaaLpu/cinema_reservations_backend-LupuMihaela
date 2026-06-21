package com.uni.backend.service;

import com.uni.backend.entity.Movie;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // mockito for junit5
public class MovieServiceImplTest {
    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        // run before every test to populate with data
        testMovie = new Movie();

        testMovie.setId(1L);
        testMovie.setTitle("Inception");
        testMovie.setStudio("Warner Bros");
        testMovie.setDuration(148);
        testMovie.setRating(8.8);
    }

    @Test
    void createMovie_ShouldReturnSavedMovie() {
        // arrange
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // act
        Movie savedMovie = movieService.createMovie(testMovie);

        // assert
        assertNotNull(savedMovie);
        assertEquals("Inception", savedMovie.getTitle());
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    void getAllMovies_ShouldReturnAllMovies_WhenExists() {
        // arrange
        when(movieRepository.findAll()).thenReturn(List.of(testMovie));

        // act
        List<Movie> allMovies = movieService.getAllMovies();

        // assert
        assertNotNull(allMovies);
        assertTrue(allMovies.contains(testMovie));
        assertEquals(1, allMovies.size());
    }

    @Test
    void getMovieById_ShouldReturnMovie_WhenExists() {
        // arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));

        // act
        Movie foundMovie = movieService.getMovieById(1L);

        // assert
        assertNotNull(foundMovie);
        assertEquals(1L, foundMovie.getId());
    }

    @Test
    void getMovieById_ShouldThrowException_WhenNotExists() {
        // arrange
        when(movieRepository.findById(2L)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(2L));
    }

    @Test
    void updateMovie_ShouldReturnSavedMovie() {
        // arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));

        Movie updateRequest = new Movie();
        updateRequest.setTitle("Titanic");
        updateRequest.setStudio("Lightstorm Entertainment");
        updateRequest.setRating(7.9);
        updateRequest.setDuration(195);

        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // act
        Movie updatedMovie = movieService.updateMovie(updateRequest, 1L);

        // assert
        assertNotNull(updatedMovie);
        assertEquals(1L, updatedMovie.getId());
        assertEquals("Titanic", updatedMovie.getTitle()); // fields updated by service

        verify(movieRepository, times(1)).findById(1L);
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    void updateMovie_ShouldThrowException_WhenNotExists() {
        // arrange
        when(movieRepository.findById(2L)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(2L));
    }

    @Test
    void deleteMovie_ShouldDeleteMovie_WhenExists() {
        // arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));

        // act
        movieService.deleteMovie(1L);

        // assert
        verify(movieRepository, times(1)).findById(1L);
        verify(movieRepository, times(1)).delete(testMovie);
    }

    @Test
    void deleteMovie_ShouldThrowException_WhenNotExists() {
        // arrange
        when(movieRepository.findById(2L)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.deleteMovie(2L));
    }

    @Test
    void findPaginated_ShouldReturnMoviePage() {
        // arrange
        int pageNo = 1;
        int pageSize = 10;
        String sortField = "title";
        String sortDirection = "ASC";

        Page<Movie> moviePage = new PageImpl<>(List.of(testMovie));
        when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);

        // act
        Page<Movie> result = movieService.findPaginated(pageNo, pageSize, sortField, sortDirection);

        // assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Inception", result.getContent().get(0).getTitle());
        verify(movieRepository, times(1)).findAll(any(Pageable.class));
    }
}
