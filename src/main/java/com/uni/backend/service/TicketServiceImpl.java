package com.uni.backend.service;

import com.uni.backend.entity.Screening;
import com.uni.backend.entity.Ticket;
import com.uni.backend.entity.User;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.repository.ScreeningRepository;
import com.uni.backend.repository.TicketRepository;
import com.uni.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ScreeningRepository screeningRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Ticket buyTicket(Ticket ticket) {
        log.info("Buy a new ticket {}...", ticket.getId());

        log.info("Check if the cinema room is full or not...");
        Screening screening = screeningRepository.findById(ticket.getScreening().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", ticket.getScreening().getId()));

        log.info("Count the sold tickets...");
        int soldTickets = ticketRepository.getTicketsByScreeningId(screening.getId()).size();
        if (soldTickets >= screening.getCinemaRoom().getCapacity()) {
            throw new IllegalStateException("We are sorry but the cinema room is full!");
        }

        log.info("Set the current logged-in user...");
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));
        ticket.setUser(currentUser);

        ticket.setPurchaseDate(LocalDateTime.now());

        ticket.setScreening(screening);

        log.info("Compute price...");
        float basePrice = 25.0f; // standard price

        if ("CHILD".equals(ticket.getTicketType())) {
            ticket.setPrice(basePrice * 0.5f); // discount 50%
        } else if ("SENIOR".equals(ticket.getTicketType())) {
            ticket.setPrice(basePrice * 0.7f); // 30% discount
        } else {
            ticket.setPrice(basePrice); // standard price for adult
        }

        return ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> getAllTickets() {
        log.info("Get all tickets...");
        return ticketRepository.findAll();
    }

    @Override
    public Ticket getTicketById(Long id) {
        log.info("Get ticket with id {}...", id);
        return ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Ticket with id {} not found.", id);
                    return new ResourceNotFoundException("Ticket", "id", id);
                });
    }

    @Override
    public List<Ticket> getAllTicketsByScreeningId(Long id) {
        log.info("Get all tickets by screening id {}...", id);

        log.debug("Check if the screening id exists or throw an arrow...");
        screeningRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Screening with ID {} was not found in the database", id);
                    return new ResourceNotFoundException("Screening", "id", id);
                });
        return ticketRepository.getTicketsByScreeningId(id);
    }

    @Override
    public Page<Ticket> findPaginatedForUser(int pageNo, int pageSize, String sortField, String sortDirection, String username) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

        return ticketRepository.findByUserUsername(username, pageable);
    }

    @Override
    @Transactional
    public Ticket updateTicketById(Ticket ticket, Long id) {
        log.info("Updating ticket with id {}...", id);

        log.debug("Check if the ticket exists else throw an error...");
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Movie with ID {} was not found in the database", id);
                    return new ResourceNotFoundException("Ticket", "id", id);
                });

        Screening screening = screeningRepository.findById(ticket.getScreening().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", ticket.getScreening().getId()));

        log.info("Update the fields of the ticket {}...", id);
        existingTicket.setSeatRow(ticket.getSeatRow());
        existingTicket.setSeatNumber(ticket.getSeatNumber());
        existingTicket.setTicketType(ticket.getTicketType());
        existingTicket.setScreening(screening);

        // purchaseDate remains exactly the same

        log.info("Recalculate the price of the ticket...");
        float basePrice = 25.0f;
        if ("CHILD".equals(ticket.getTicketType())) {
            existingTicket.setPrice(basePrice * 0.5f);
        } else if ("SENIOR".equals(ticket.getTicketType())) {
            existingTicket.setPrice(basePrice * 0.7f);
        } else {
            existingTicket.setPrice(basePrice);
        }

        log.info("Save and returned the updated movie...");
        return ticketRepository.save(existingTicket);
    }

    @Override
    @Transactional
    public void deleteTicketById(Long id) {
        log.debug("Deleting ticket with id {}...", id);

        log.debug("Check if the ticket exists or throw an error...");
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Ticket with ID {} was not found in the database", id);
                    return new ResourceNotFoundException("Ticket", "id", id);
                });
        ticketRepository.delete(existingTicket);
    }

    @Override
    public Page<Ticket> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

        return ticketRepository.findAll(pageable);
    }
}
