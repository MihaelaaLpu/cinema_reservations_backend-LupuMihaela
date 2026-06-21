package com.uni.backend.repository;

import com.uni.backend.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    List<Screening> getScreeningsByCinemaRoomId(Long id);
    List<Screening> getScreeningsByMovieId(Long id);
    void deleteAll();
}
