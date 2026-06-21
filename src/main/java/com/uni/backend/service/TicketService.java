package com.uni.backend.service;

import com.uni.backend.entity.Ticket;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TicketService {
    // create
    Ticket buyTicket(Ticket ticket);

    // read
    List<Ticket> getAllTickets();
    Ticket getTicketById(Long id);
    List<Ticket> getAllTicketsByScreeningId(Long id);
    Page<Ticket> findPaginatedForUser(int pageNo, int pageSize, String sortField, String sortDirection, String username);

    // update
    Ticket updateTicketById(Ticket ticket, Long id);

    // delete
    void deleteTicketById(Long id);

    Page<Ticket> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection);
}
