package com.uni.backend.service;

import com.uni.backend.entity.CinemaRoom;
import com.uni.backend.entity.Movie;
import com.uni.backend.entity.Screening;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.exception.ScreeningOverlapException;
import com.uni.backend.repository.CinemaRoomRepository;
import com.uni.backend.repository.MovieRepository;
import com.uni.backend.repository.ScreeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScreeningServiceImplTest {
    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private CinemaRoomRepository cinemaRoomRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private ScreeningServiceImpl screeningService;

    private Screening testScreening;
    private Movie testMovie;
    private CinemaRoom testRoom;

    @BeforeEach
    void setUp() {
        // set up the movie
        testMovie = new Movie();
        testMovie.setId(1L);
        testMovie.setTitle("Inception");
        testMovie.setDuration(120); // 2 hours long

        // set up the cinema room
        testRoom = new CinemaRoom();
        testRoom.setId(1L);
        testRoom.setRoomCode("A01");

        // set up the screening
        testScreening = new Screening();
        testScreening.setId(1L);
        testScreening.setMovie(testMovie);
        testScreening.setCinemaRoom(testRoom);
        testScreening.setStartTime(LocalDateTime.of(2026, 6, 25, 10, 0));
    }

    @Test
    void sheduleScreening_ShouldReturnSavedScreening_WhenNoOverlap() {
        // arrange
        when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(screeningRepository.getScreeningsByCinemaRoomId(1L)).thenReturn(new ArrayList<>());
        when(screeningRepository.save(any(Screening.class))).thenReturn(testScreening);

        // act
        Screening savedScreening = screeningService.sheduleScreening(testScreening);

        // assert
        assertNotNull(savedScreening);
        assertEquals(1L, savedScreening.getId());
        verify(screeningRepository, times(1)).save(any(Screening.class));
    }

    @Test
    void sheduleScreening_ShouldThrowException_WhenOverlapExists() {
        // arrange: create an existing screening that overlaps
        Screening overlappingScreening = new Screening();
        overlappingScreening.setId(2L);
        overlappingScreening.setMovie(testMovie); // 120 min
        overlappingScreening.setStartTime(LocalDateTime.of(2026, 6, 25, 11, 0));

        when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(screeningRepository.getScreeningsByCinemaRoomId(1L)).thenReturn(List.of(overlappingScreening));

        // act & assert
        assertThrows(ScreeningOverlapException.class, () -> screeningService.sheduleScreening(testScreening));
        verify(screeningRepository, never()).save(any(Screening.class));
    }

    @Test
    void getAllScreenings_ShouldReturnAllScreenings() {
        when(screeningRepository.findAll()).thenReturn(List.of(testScreening));

        List<Screening> result = screeningService.getAllScreenings();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(screeningRepository, times(1)).findAll();
    }

    @Test
    void getScreeningByScreeningId_ShouldReturnScreening() {
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        Screening result = screeningService.getScreeningByScreeningId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getScreeningByRoomId_ShouldReturnScreenings() {
        when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(screeningRepository.getScreeningsByCinemaRoomId(1L)).thenReturn(List.of(testScreening));

        List<Screening> result = screeningService.getScreeningByRoomId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getScreeningByMovieId_ShouldReturnScreenings() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(screeningRepository.getScreeningsByMovieId(1L)).thenReturn(List.of(testScreening));

        List<Screening> result = screeningService.getScreeningByMovieId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateScreening_ShouldReturnUpdatedScreening_WhenNoOverlap() {
        // arrange
        Screening updateRequest = new Screening();
        updateRequest.setMovie(testMovie);
        updateRequest.setCinemaRoom(testRoom);
        // Rescheduling to 2:00 PM
        updateRequest.setStartTime(LocalDateTime.of(2026, 6, 25, 14, 0));

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(screeningRepository.getScreeningsByCinemaRoomId(1L)).thenReturn(new ArrayList<>());
        when(screeningRepository.save(any(Screening.class))).thenReturn(testScreening);

        // act
        Screening updatedScreening = screeningService.updateScreening(updateRequest, 1L);

        // assert
        assertNotNull(updatedScreening);
        verify(screeningRepository, times(1)).save(any(Screening.class));
    }

    @Test
    void updateScreening_ShouldSkipSelfAndSave_WhenComparingToOwnExistingTime() {
        // arrange
        Screening updateRequest = new Screening();
        updateRequest.setMovie(testMovie);
        updateRequest.setCinemaRoom(testRoom);
        // keeping the time the same to see if it skips itself
        updateRequest.setStartTime(LocalDateTime.of(2026, 6, 25, 10, 0));

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // mock returning itself in the room's list
        when(screeningRepository.getScreeningsByCinemaRoomId(1L)).thenReturn(List.of(testScreening));
        when(screeningRepository.save(any(Screening.class))).thenReturn(testScreening);

        // act
        Screening updatedScreening = screeningService.updateScreening(updateRequest, 1L);

        // assert
        assertNotNull(updatedScreening);
        verify(screeningRepository, times(1)).save(any(Screening.class)); // It should save without throwing an exception!
    }

    @Test
    void deleteScreening_ShouldDelete_WhenExists() {
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        screeningService.deleteScreening(1L);

        verify(screeningRepository, times(1)).findById(1L);
        verify(screeningRepository, times(1)).delete(testScreening);
        assertNull(testScreening.getCinemaRoom()); // verifies your existingScreening.setCinemaRoom(null) logic
    }

    @Test
    void deleteScreening_ShouldThrowException_WhenNotExists() {
        when(screeningRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> screeningService.deleteScreening(2L));
        verify(screeningRepository, never()).delete(any(Screening.class));
    }

    @Test
    void findPaginated_ShouldReturnScreeningPage() {
        // arrange
        int pageNo = 1;
        int pageSize = 10;
        String sortField = "startTime";
        String sortDirection = "ASC";

        Page<Screening> screeningPage = new PageImpl<>(List.of(testScreening));
        when(screeningRepository.findAll(any(Pageable.class))).thenReturn(screeningPage);

        // act
        Page<Screening> result = screeningService.findPaginated(pageNo, pageSize, sortField, sortDirection);

        // assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(screeningRepository, times(1)).findAll(any(Pageable.class));
    }
}
