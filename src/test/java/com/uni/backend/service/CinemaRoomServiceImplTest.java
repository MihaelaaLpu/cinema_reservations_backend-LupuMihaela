package com.uni.backend.service;

import com.uni.backend.entity.CinemaRoom;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.repository.CinemaRoomRepository;
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
public class CinemaRoomServiceImplTest {

    @Mock
    private CinemaRoomRepository cinemaRoomRepository;

    @InjectMocks
    private CinemaRoomServiceImpl cinemaRoomService;

    private CinemaRoom testRoom;

    @BeforeEach
    void setUp() {
        // Run before every test to populate with data
        testRoom = new CinemaRoom();
        testRoom.setId(1L);
        testRoom.setRoomCode("A01");
        testRoom.setCapacity(50);
        testRoom.setIs3D(true);
    }

    @Test
    void createCinemaRoom_ShouldReturnSavedRoom() {
        // arrange
        when(cinemaRoomRepository.save(any(CinemaRoom.class))).thenReturn(testRoom);

        // act
        CinemaRoom savedRoom = cinemaRoomService.createCinemaRoom(testRoom);

        // assert
        assertNotNull(savedRoom);
        assertEquals("A01", savedRoom.getRoomCode());
        verify(cinemaRoomRepository, times(1)).save(any(CinemaRoom.class));
    }

    @Test
    void getAllCinemaRooms_ShouldReturnAllRooms_WhenExists() {
        // arrange
        when(cinemaRoomRepository.findAll()).thenReturn(List.of(testRoom));

        // act
        List<CinemaRoom> allRooms = cinemaRoomService.getAllCinemaRooms();

        // assert
        assertNotNull(allRooms);
        assertTrue(allRooms.contains(testRoom));
        assertEquals(1, allRooms.size());
        verify(cinemaRoomRepository, times(1)).findAll();
    }

    @Test
    void getCinemaRoomById_ShouldReturnRoom_WhenExists() {
        // arrange
        when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // act
        CinemaRoom foundRoom = cinemaRoomService.getCinemaRoomById(1L);

        // assert
        assertNotNull(foundRoom);
        assertEquals(1L, foundRoom.getId());
        assertEquals("A01", foundRoom.getRoomCode());
        verify(cinemaRoomRepository, times(1)).findById(1L);
    }

    @Test
    void getCinemaRoomById_ShouldThrowException_WhenNotExists() {
        // arrange
        when(cinemaRoomRepository.findById(2L)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> cinemaRoomService.getCinemaRoomById(2L));
        verify(cinemaRoomRepository, times(1)).findById(2L);
    }

    @Test
    void getCinemaRoomByRoomCode_ShouldReturnRoom_WhenExists() {
        // arrange
        when(cinemaRoomRepository.findCinemaRoomByRoomCode("A01")).thenReturn(Optional.of(testRoom));

        // act
        CinemaRoom foundRoom = cinemaRoomService.getCinemaRoomByRoomCode("A01");

        // assert
        assertNotNull(foundRoom);
        assertEquals("A01", foundRoom.getRoomCode());
        verify(cinemaRoomRepository, times(1)).findCinemaRoomByRoomCode("A01");
    }

    @Test
    void getCinemaRoomByRoomCode_ShouldThrowException_WhenNotExists() {
        // arrange
        when(cinemaRoomRepository.findCinemaRoomByRoomCode("Z99")).thenReturn(Optional.empty());

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> cinemaRoomService.getCinemaRoomByRoomCode("Z99"));
        verify(cinemaRoomRepository, times(1)).findCinemaRoomByRoomCode("Z99");
    }

    @Test
    void updateCinemaRoom_ShouldReturnSavedRoom() {
        // arrange
        when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        CinemaRoom updateRequest = new CinemaRoom();
        updateRequest.setRoomCode("B02");
        updateRequest.setCapacity(100);
        updateRequest.setIs3D(false);

        when(cinemaRoomRepository.save(any(CinemaRoom.class))).thenReturn(testRoom);

        // act
        CinemaRoom updatedRoom = cinemaRoomService.updateCinemaRoom(updateRequest, 1L);

        // assert
        assertNotNull(updatedRoom);
        assertEquals(1L, updatedRoom.getId());
        assertEquals("B02", updatedRoom.getRoomCode()); // fields updated by service
        assertEquals(100, updatedRoom.getCapacity());
        assertFalse(updatedRoom.getIs3D());

        verify(cinemaRoomRepository, times(1)).findById(1L);
        verify(cinemaRoomRepository, times(1)).save(any(CinemaRoom.class));
    }

    @Test
    void updateCinemaRoom_ShouldThrowException_WhenNotExists() {
        // arrange
        when(cinemaRoomRepository.findById(2L)).thenReturn(Optional.empty());

        CinemaRoom updateRequest = new CinemaRoom();
        updateRequest.setRoomCode("B02");

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> cinemaRoomService.updateCinemaRoom(updateRequest, 2L));
        verify(cinemaRoomRepository, times(1)).findById(2L);
        verify(cinemaRoomRepository, never()).save(any());
    }

    @Test
    void deleteCinemaRoom_ShouldDeleteRoom_WhenExists() {
        // arrange
        when(cinemaRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // act
        cinemaRoomService.deleteCinemaRoom(1L);

        // assert
        verify(cinemaRoomRepository, times(1)).findById(1L);
        verify(cinemaRoomRepository, times(1)).delete(testRoom);
    }

    @Test
    void deleteCinemaRoom_ShouldThrowException_WhenNotExists() {
        // arrange
        when(cinemaRoomRepository.findById(2L)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> cinemaRoomService.deleteCinemaRoom(2L));
        verify(cinemaRoomRepository, times(1)).findById(2L);
        verify(cinemaRoomRepository, never()).delete(any());
    }

    @Test
    void findPaginated_ShouldReturnCinemaRoomPage() {
        // arrange
        int pageNo = 1;
        int pageSize = 10;
        String sortField = "roomCode";
        String sortDirection = "ASC";

        Page<CinemaRoom> roomPage = new PageImpl<>(List.of(testRoom));
        when(cinemaRoomRepository.findAll(any(Pageable.class))).thenReturn(roomPage);

        // act
        Page<CinemaRoom> result = cinemaRoomService.findPaginated(pageNo, pageSize, sortField, sortDirection);

        // assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("A01", result.getContent().get(0).getRoomCode());
        verify(cinemaRoomRepository, times(1)).findAll(any(Pageable.class));
    }
}
