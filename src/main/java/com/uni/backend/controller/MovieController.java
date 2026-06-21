package com.uni.backend.controller;

import com.uni.backend.entity.Movie;
import com.uni.backend.service.MovieService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping
    public String showMovies(Model model) {
        // model is the variable for html
        return findPaginated(1, "title", "asc", model);
    }

    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable(value = "pageNo") int pageNo,
                                @RequestParam(value = "sortField") String sortField,
                                @RequestParam(value = "sortDir") String sortDir,
                                Model model) {

        int pageSize = 5;

        Page<Movie> page = movieService.findPaginated(pageNo, pageSize, sortField, sortDir);
        List<Movie> listMovies = page.getContent();

        // send to html all variable for the buttons next/ prev and the table
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc"); // change sorting at click

        model.addAttribute("movies", listMovies);

        return "movies/list";
    }

    @GetMapping("/new")
    public String createMovieForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "movies/create"; // the file list.html from templates/movies/
    }

    @PostMapping
    public String saveMovie(@Valid @ModelAttribute("movie") Movie movie, BindingResult result) {
        // if the entered data is not valid (@NotNull, @Min etc.)
        if (result.hasErrors()) {
            return "movies/create"; // stay on the page to show the error
        }
        movieService.createMovie(movie);
        return "redirect:/movies"; // redirect to the list.html
    }

    @GetMapping("/edit/{id}")
    public String editMovieForm(@PathVariable Long id, Model model) {
        model.addAttribute("movie", movieService.getMovieById(id));
        return "movies/edit"; // the file edit.html
    }

    @PostMapping("/{id}")
    public String updateMovie(@PathVariable Long id, @Valid @ModelAttribute("movie") Movie movie, BindingResult result) {
        if (result.hasErrors()) {
            return "movies/edit";
        }
        movieService.updateMovie(movie, id);
        return "redirect:/movies";
    }

    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/movies";
    }
}
