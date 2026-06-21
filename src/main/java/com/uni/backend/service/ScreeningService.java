package com.uni.backend.service;

import com.uni.backend.entity.Screening;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ScreeningService {
    // create
    Screening sheduleScreening(Screening screening);

    // read
    List<Screening> getAllScreenings();
    Screening getScreeningByScreeningId(Long id);
    List<Screening> getScreeningByRoomId(Long id);
    List<Screening> getScreeningByMovieId(Long id);

    // update
    Screening updateScreening(Screening screening, Long id);

    // delete
    void deleteScreening(Long id);

    Page<Screening> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection);
}
