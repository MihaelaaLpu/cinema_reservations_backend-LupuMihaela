package com.uni.backend.controller;

import com.uni.backend.entity.CinemaRoom;
import com.uni.backend.service.CinemaRoomService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/cinema-rooms")
@RequiredArgsConstructor
public class CinemaRoomController {
    private final CinemaRoomService cinemaRoomService;

    @GetMapping
    public String showCinemaRooms(Model model) {
        return findPaginated(1, "roomCode", "asc", model);
    }

    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable(value = "pageNo") int pageNo,
                                @RequestParam(value = "sortField") String sortField,
                                @RequestParam(value = "sortDir") String sortDir,
                                Model model) {

        int pageSize = 5;

        Page<CinemaRoom> page = cinemaRoomService.findPaginated(pageNo, pageSize, sortField, sortDir);
        List<CinemaRoom> cinemaRooms = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc"); // // change sorting at click

        model.addAttribute("cinemaRooms", cinemaRooms);

        return "cinema-rooms/list";
    }

    @GetMapping("/new")
    public String createCinemaRoom(Model model) {
        model.addAttribute("cinemaRoom", new CinemaRoom());
        return "cinema-rooms/create";
    }

    @PostMapping
    public String saveCinemaRoom(@Valid @ModelAttribute("cinemaRoom") CinemaRoom cinemaRoom, BindingResult result) {
        if (result.hasErrors()) {
            return "cinema-rooms/create"; // stay on the page to show the error
        }
        cinemaRoomService.createCinemaRoom(cinemaRoom);
        return "redirect:/cinema-rooms"; // redirect to the list.html
    }

    @GetMapping("/edit/{id}")
    public String editCinemaRoomForm(@PathVariable Long id, Model model) {
        model.addAttribute("cinemaRoom", cinemaRoomService.getCinemaRoomById(id));
        return "cinema-rooms/edit";
    }

    @PostMapping("/{id}")
    public String updateCinemaRoom(@PathVariable Long id, @Valid @ModelAttribute("cinemaRoom") CinemaRoom cinemaRoom, BindingResult result) {
        if (result.hasErrors()) {
            return "cinema-rooms/edit";
        }
        cinemaRoomService.updateCinemaRoom(cinemaRoom, id);
        return "redirect:/cinema-rooms";
    }

    @GetMapping("/delete/{id}")
    public String deleteCinemaRoom(@PathVariable Long id) {
        cinemaRoomService.deleteCinemaRoom(id);
        return "redirect:/cinema-rooms";
    }
}
