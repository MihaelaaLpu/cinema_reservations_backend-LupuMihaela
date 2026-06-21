package com.uni.backend.controller;

import com.uni.backend.entity.Screening;
import com.uni.backend.exception.ScreeningOverlapException;
import com.uni.backend.service.CinemaRoomService;
import com.uni.backend.service.MovieService;
import com.uni.backend.service.ScreeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/screenings")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;
    private final MovieService movieService;
    private final CinemaRoomService cinemaRoomService;

    @GetMapping
    public String listScreenings(Model model) {
        return findPaginated(1, "startTime", "desc", model);
    }

    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable(value = "pageNo") int pageNo,
                                @RequestParam(value = "sortField") String sortField,
                                @RequestParam(value = "sortDir") String sortDir,
                                Model model) {

        int pageSize = 5;

        Page<Screening> page = screeningService.findPaginated(pageNo, pageSize, sortField, sortDir);
        List<Screening> listScreening = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("screenings", listScreening);

        return "screenings/list";
    }

    @GetMapping("/new")
    public String createScreeningForm(Model model) {
        model.addAttribute("screening", new Screening());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("cinemaRooms", cinemaRoomService.getAllCinemaRooms());
        return "screenings/create";
    }

    @PostMapping
    public String saveScreening(@Valid @ModelAttribute("screening") Screening screening, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("cinemaRooms", cinemaRoomService.getAllCinemaRooms());
            return "screenings/create";
        }

        try {
            // attempt to save the screening
            screeningService.sheduleScreening(screening);
        } catch (ScreeningOverlapException ex) {
            // if the overlap logic fails, catch it

            // reload the dropdown lists
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("cinemaRooms", cinemaRoomService.getAllCinemaRooms());

            // pass the custom error message to the view
            model.addAttribute("overlapError", ex.getMessage());

            // return to the form
            return "screenings/create";
        }

        return "redirect:/screenings";
    }

    @GetMapping("/edit/{id}")
    public String editScreeningForm(@PathVariable Long id, Model model) {
        model.addAttribute("screening", screeningService.getScreeningByScreeningId(id));
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("cinemaRooms", cinemaRoomService.getAllCinemaRooms());
        return "screenings/edit";
    }

    @PostMapping("/{id}")
    public String updateScreening(@PathVariable Long id, @Valid @ModelAttribute("screening") Screening screening, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("cinemaRooms", cinemaRoomService.getAllCinemaRooms());
            return "screenings/edit";
        }

        try {
            screeningService.updateScreening(screening, id);
        } catch (ScreeningOverlapException ex) {
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("cinemaRooms", cinemaRoomService.getAllCinemaRooms());
            model.addAttribute("overlapError", ex.getMessage());
            return "screenings/edit";
        }

        return "redirect:/screenings";
    }

    @GetMapping("/delete/{id}")
    public String deleteScreening(@PathVariable Long id) {
        screeningService.deleteScreening(id);
        return "redirect:/screenings";
    }
}