package com.saathisquare.societyservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @GetMapping
    public ResponseEntity<String> healthCheck() {
    	log.info("Society Service Application is running..");
        return ResponseEntity.ok("Society Service Application is running...");
    }

}

