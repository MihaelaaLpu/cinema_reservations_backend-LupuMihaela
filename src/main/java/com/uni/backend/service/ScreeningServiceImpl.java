package com.uni.backend.service;

import com.uni.backend.entity.CinemaRoom;
import com.uni.backend.entity.Movie;
import com.uni.backend.entity.Screening;
import com.uni.backend.entity.Ticket;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.exception.ScreeningOverlapException;
import com.uni.backend.repository.CinemaRoomRepository;
import com.uni.backend.repository.MovieRepository;
import com.uni.backend.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningServiceImpl implements ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final CinemaRoomRepository cinemaRoomRepository;
    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public Screening sheduleScreening(Screening screening) {
        log.debug("Schedule new screening {}...", screening.getId());

        CinemaRoom room = cinemaRoomRepository.findById(screening.getCinemaRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cinema Room", "id", screening.getCinemaRoom().getId()));

        Movie movie = movieRepository.findById(screening.getMovie().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", screening.getMovie().getId()));

        log.info("Compute when the movie starts and when it ends...");
        LocalDateTime newStartTime = screening.getStartTime();
        LocalDateTime newEndTime = newStartTime.plusMinutes(movie.getDuration());

        log.info("Take all the screenings from that cinema room...");
        List<Screening> existingScreeningsInRoom = screeningRepository.getScreeningsByCinemaRoomId(room.getId());

        log.info("Check for overlaps...");
        for (Screening existing : existingScreeningsInRoom) {
            LocalDateTime existingStartTime = existing.getStartTime();
            LocalDateTime existingEndTime = existingStartTime.plusMinutes(existing.getMovie().getDuration());

            log.debug("Check time intervals (StartA < EndB) and (EndA > StartB)...");
            if (newStartTime.isBefore(existingEndTime) && newEndTime.isAfter(existingStartTime)) {
                throw new ScreeningOverlapException("The room " + room.getRoomCode() +
                        " is already booked during this time slot for the movie: " + existing.getMovie().getTitle());
            }
        }

        screening.setCinemaRoom(room);
        screening.setMovie(movie);

        return screeningRepository.save(screening);
    }

    @Override
    public List<Screening> getAllScreenings() {
        log.debug("Getting all screenings...");
        return screeningRepository.findAll();
    }

    @Override
    public Screening getScreeningByScreeningId(Long id) {
        log.debug("Getting screening with id {}...", id);
        return screeningRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Screening with id {} not found.", id);
                    return new ResourceNotFoundException("Screening", "id", id);
                });
    }

    @Override
    public List<Screening> getScreeningByRoomId(Long id) {
        log.info("Check if the screening exists else throw an error...");
        cinemaRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema Room", "id", id));
        return screeningRepository.getScreeningsByCinemaRoomId(id);
    }

    @Override
    public List<Screening> getScreeningByMovieId(Long id) {
        log.info("Check if the screening id exists else throw an error...");
        movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        return screeningRepository.getScreeningsByMovieId(id);
    }

    @Override
    @Transactional
    public Screening updateScreening(Screening screening, Long id) {
        log.info("Check if the screening exists else throw an error...");
        Screening existingScreening = screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));

        Movie movie = movieRepository.findById(screening.getMovie().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", screening.getMovie().getId()));

        CinemaRoom room = cinemaRoomRepository.findById(screening.getCinemaRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cinema Room", "id", screening.getCinemaRoom().getId()));

        log.info("Compute the new time interval...");
        LocalDateTime newStartTime = screening.getStartTime();
        LocalDateTime newEndTime = newStartTime.plusMinutes(movie.getDuration());

        log.info("Get screenings from that room...");
        List<Screening> existingScreeningsInRoom = screeningRepository.getScreeningsByCinemaRoomId(room.getId());

        log.info("Check for overlaps...");
        for (Screening existing : existingScreeningsInRoom) {
            log.info("Skip the current screening...");
            if (existing.getId().equals(existingScreening.getId())) {
                continue;
            }

            LocalDateTime existingStartTime = existing.getStartTime();
            LocalDateTime existingEndTime = existingStartTime.plusMinutes(existing.getMovie().getDuration());

            if (newStartTime.isBefore(existingEndTime) && newEndTime.isAfter(existingStartTime)) {
                throw new ScreeningOverlapException("The room " + room.getRoomCode() +
                        " is already booked during this time slot for the movie: " + existing.getMovie().getTitle());
            }
        }

        existingScreening.setStartTime(screening.getStartTime());
        existingScreening.setMovie(movie);
        existingScreening.setCinemaRoom(room);

        return screeningRepository.save(existingScreening);
    }

    @Override
    @Transactional
    public void deleteScreening(Long id) {
        log.info("Deleting screening with id {}...", id);

        log.info("Check if the screening exists else throw an error...");
        Screening existingScreening = screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));

        existingScreening.setCinemaRoom(null);

        screeningRepository.delete(existingScreening);
    }

    @Override
    public Page<Screening> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

        return screeningRepository.findAll(pageable);
    }
}
