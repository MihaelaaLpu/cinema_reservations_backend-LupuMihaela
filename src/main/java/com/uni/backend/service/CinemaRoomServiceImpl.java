package com.uni.backend.service;

import com.uni.backend.entity.CinemaRoom;
import com.uni.backend.entity.Screening;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.repository.CinemaRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CinemaRoomServiceImpl implements CinemaRoomService {

    private final CinemaRoomRepository cinemaRoomRepository;

    @Override
    @Transactional
    public CinemaRoom createCinemaRoom(CinemaRoom cinemaRoom) {
        log.info("Creating cinema room: {}", cinemaRoom.getRoomCode());
        return cinemaRoomRepository.save(cinemaRoom);
    }

    @Override
    public List<CinemaRoom> getAllCinemaRooms() {
        log.info("Retrieving all cinema rooms...");
        return cinemaRoomRepository.findAll();
    }

    @Override
    public CinemaRoom getCinemaRoomById(Long id) {
        log.debug("Retrieving cinema room with id {}...", id);
        return cinemaRoomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cinema room with ID {} was not found in the database", id);
                    return new ResourceNotFoundException("Cinema room", "id", id);
                });
    }

    @Override
    public CinemaRoom getCinemaRoomByRoomCode(String roomCode) {
        log.debug("Getting cinema room by room code {}", roomCode);
        return cinemaRoomRepository.findCinemaRoomByRoomCode(roomCode)
                .orElseThrow(() -> {
                    log.error("The cinema room with the code {} was not found in the database!", roomCode);
                    return new ResourceNotFoundException("Cinema room", "id", roomCode);
                });
    }

    @Override
    @Transactional
    public CinemaRoom updateCinemaRoom(CinemaRoom cinemaRoom, Long id) {
        log.debug("Updating cinema room with id {}...", id);

        log.debug("Check if the cinema room exists else throw an error...");
        CinemaRoom existingRoom = cinemaRoomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cinema room with ID {} was not found in the database", id);
                    return new ResourceNotFoundException("Cinema room", "id", id);
                });

        log.info("Update the fields for cinema room: {}...", cinemaRoom.getRoomCode());
        existingRoom.setRoomCode(cinemaRoom.getRoomCode());
        existingRoom.setCapacity(cinemaRoom.getCapacity());
        existingRoom.setIs3D(cinemaRoom.getIs3D());

        log.info("Save and returned the updated cinema room: {}...", cinemaRoom.getRoomCode());
        return cinemaRoomRepository.save(existingRoom);
    }

    @Override
    @Transactional
    public void deleteCinemaRoom(Long id) {
        log.debug("Deleting cinema room with id {}...", id);

        log.debug("Check if the cinema room exists else throw an error...");
        CinemaRoom existingRoom = cinemaRoomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cinema room with ID {} was not found in the database", id);
                    return new ResourceNotFoundException("Cinema room", "id", id);
                });

        cinemaRoomRepository.delete(existingRoom);
    }

    @Override
    public Page<CinemaRoom> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

        return cinemaRoomRepository.findAll(pageable);
    }
}
