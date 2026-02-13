package com.saathisquare.societyservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saathisquare.societyservice.dto.request.AssignUserRequest;
import com.saathisquare.societyservice.dto.request.CreateFlatRequest;
import com.saathisquare.societyservice.dto.response.FlatResponse;
import com.saathisquare.societyservice.service.FlatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flats")
public class FlatController {

    private final FlatService flatService;

    public FlatController(FlatService flatService) {
        this.flatService = flatService;
    }

    @PostMapping
    public ResponseEntity<FlatResponse> createFlat(@RequestBody @Valid CreateFlatRequest request) {
    	 return ResponseEntity.ok(flatService.createFlat(request));
    }

    @PutMapping("/assign-user")
    public ResponseEntity<com.saathisquare.societyservice.util.Response<String>> assignUser(@RequestBody @Valid AssignUserRequest request) {
        return ResponseEntity.ok(flatService.assignUserToFlat(request));
    }
}
