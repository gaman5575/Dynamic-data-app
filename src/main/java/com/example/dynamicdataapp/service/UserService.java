package com.example.dynamicdataapp.service;

import com.example.dynamicdataapp.model.User;
import com.example.dynamicdataapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registerUser(String username, String password) {
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            return false; // Username already taken
        }

        // Encode the password and save the user
        User user = new User(username, passwordEncoder.encode(password));
        userRepository.save(user);
        return true;
    }
}