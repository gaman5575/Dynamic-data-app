package com.example.dynamicdataapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.dynamicdataapp.repository.TableDefinitionRepository; // Adjust the package as needed
import com.example.dynamicdataapp.model.TableDefinition; // Adjust the package as needed

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final TableDefinitionRepository tableDefinitionRepository;

    @Autowired
    public AuthController(TableDefinitionRepository tableDefinitionRepository) {
        this.tableDefinitionRepository = tableDefinitionRepository;
    }
    // Handle root URL
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/login"; // Redirect to login page
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        logger.info("Rendering dashboard");
        // Fetch all tables from the database
        Iterable<TableDefinition> tables = tableDefinitionRepository.findAll();
        // Add the tables to the model for the template to use
        model.addAttribute("tables", tables);
        return "dashboard";
    }
}