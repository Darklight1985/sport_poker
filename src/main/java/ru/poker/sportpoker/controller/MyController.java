package ru.poker.sportpoker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offline")
public class MyController {

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "This is an admin endpoint";
    }
}
