package com.uni.backend.service;

import com.uni.backend.entity.CinemaRoom;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CinemaRoomService {
    // create
    CinemaRoom createCinemaRoom(CinemaRoom cinemaRoom);

    // read
    List<CinemaRoom> getAllCinemaRooms();
    CinemaRoom getCinemaRoomById(Long id);
    CinemaRoom getCinemaRoomByRoomCode(String roomCode);

    // update
    CinemaRoom updateCinemaRoom(CinemaRoom cinemaRoom, Long id);

    // delete
    void deleteCinemaRoom(Long id);

    Page<CinemaRoom> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection);
}