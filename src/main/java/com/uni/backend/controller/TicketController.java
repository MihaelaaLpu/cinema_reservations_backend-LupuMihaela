package com.uni.backend.controller;

import com.uni.backend.entity.Ticket;
import com.uni.backend.service.ScreeningService;
import com.uni.backend.service.TicketService;
import com.uni.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final ScreeningService screeningService;

    @GetMapping
    public String listTickets(Model model, Principal principal) {
        return findPaginated(1, "purchaseDate", "desc", model, principal);
    }

    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable(value = "pageNo") int pageNo,
                                @RequestParam(value = "sortField") String sortField,
                                @RequestParam(value = "sortDir") String sortDir,
                                Model model,
                                Principal principal) {

        int pageSize = 5;
        Page<Ticket> page;

        // check if the logged-in user is an admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // the ADMIN can see all the tickets but a regular USER can see only tickets he bought
        if (isAdmin) {
            page = ticketService.findPaginated(pageNo, pageSize, sortField, sortDir);
        } else {
            page = ticketService.findPaginatedForUser(pageNo, pageSize, sortField, sortDir, principal.getName());
        }

        List<Ticket> listTickets = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("tickets", listTickets);

        return "tickets/list";
    }

    @GetMapping("/new")
    public String createTicketForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("screenings", screeningService.getAllScreenings());
        return "tickets/create";
    }

    @PostMapping
    public String saveTicket(@Valid @ModelAttribute("ticket") Ticket ticket, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("screenings", screeningService.getAllScreenings());
            return "tickets/create";
        }
        ticketService.buyTicket(ticket);
        return "redirect:/tickets";
    }

    @GetMapping("/edit/{id}")
    public String editTicketForm(@PathVariable Long id, Model model) {
        model.addAttribute("ticket", ticketService.getTicketById(id));
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("screenings", screeningService.getAllScreenings());
        return "tickets/edit";
    }

    @PostMapping("/{id}")
    public String updateTicket(@PathVariable Long id, @Valid @ModelAttribute("ticket") Ticket ticket, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("screenings", screeningService.getAllScreenings());
            return "tickets/edit";
        }
        ticketService.updateTicketById(ticket, id);
        return "redirect:/tickets";
    }

    @GetMapping("/delete/{id}")
    public String deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicketById(id);
        return "redirect:/tickets";
    }
}