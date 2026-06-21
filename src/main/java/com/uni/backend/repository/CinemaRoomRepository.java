package com.uni.backend.repository;

import com.uni.backend.entity.CinemaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Long> {

    Optional<CinemaRoom> findCinemaRoomByRoomCode(String roomCode);
    void deleteAll();
}
