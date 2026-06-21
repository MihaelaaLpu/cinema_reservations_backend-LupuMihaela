package com.uni.backend.repository;

import com.uni.backend.entity.Ticket;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> getTicketsByScreeningId(Long id);
    void deleteAll();

    Page<Ticket> findByUserUsername(String username, Pageable pageable);
}
