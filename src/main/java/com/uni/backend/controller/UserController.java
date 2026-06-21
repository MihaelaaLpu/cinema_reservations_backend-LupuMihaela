package com.uni.backend.controller;

import com.uni.backend.entity.User;
import com.uni.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        return findPaginated(1, "username", "asc", model);
    }

    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable(value = "pageNo") int pageNo,
                                @RequestParam(value = "sortField") String sortField,
                                @RequestParam(value = "sortDir") String sortDir,
                                Model model) {

        int pageSize = 5;

        Page<User> page = userService.findPaginated(pageNo, pageSize, sortField, sortDir);
        List<User> listUsers = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("users", listUsers);

        return "users/list";
    }

    @GetMapping("/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "users/create";
    }

    @PostMapping
    public String saveUser(@Valid @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "users/create";
        }
        userService.createUser(user);
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "users/edit";
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id, @Valid @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "users/edit";
        }
        userService.updateUser(user, id);
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }
}