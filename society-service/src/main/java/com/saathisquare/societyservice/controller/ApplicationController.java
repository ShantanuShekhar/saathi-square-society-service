package com.saathisquare.societyservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/")
public class ApplicationController {

    @GetMapping
    public ResponseEntity<String> healthCheck() {
    	log.info("Society Service Application is running...");
        return ResponseEntity.ok("Society Service Application is running...");
    }

}

