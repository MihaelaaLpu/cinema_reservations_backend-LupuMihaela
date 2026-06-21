package com.uni.backend.controller;

import com.uni.backend.entity.User;
import com.uni.backend.entity.UserDetails;
import com.uni.backend.repository.RoleRepository;
import com.uni.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "users/auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        User user = new User();
        user.setUserDetails(new UserDetails());

        model.addAttribute("user", user);
        // fetch all roles from db and send them to the html
        model.addAttribute("allRoles", roleRepository.findAll());

        return "users/auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // if there is an error, we need to reload the roles for the form
            model.addAttribute("allRoles", roleRepository.findAll());
            return "users/auth/register";
        }
        userService.createUser(user);
        return "redirect:/login?registered";
    }
}
