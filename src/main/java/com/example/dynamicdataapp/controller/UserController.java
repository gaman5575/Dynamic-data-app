package com.example.dynamicdataapp.controller;

import com.example.dynamicdataapp.service.UserService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public UserController(UserService userService, RequestMappingHandlerMapping handlerMapping) {
        this.userService = userService;
        this.handlerMapping = handlerMapping;
    }

    @PostConstruct
    public void logMappings() {
        logger.info("Mapped endpoints: {}", handlerMapping.getHandlerMethods());
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password, Model model) {
        boolean isRegistered = userService.registerUser(username, password);
        if (isRegistered) {
            return "redirect:/login?registered";
        } else {
            model.addAttribute("errorMessage", "Username already exists. Please choose a different username.");
            return "register";
        }
    }
}