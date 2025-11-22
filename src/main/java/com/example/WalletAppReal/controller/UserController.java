package com.example.WalletAppReal.controller;


import com.example.WalletAppReal.adapters.UserAdapter;
import com.example.WalletAppReal.dto.UserRequestDTO;
import com.example.WalletAppReal.dto.UserResponseDTO;
import com.example.WalletAppReal.models.User;
import com.example.WalletAppReal.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        User user = userService.createUser(userRequestDTO);
        UserResponseDTO userResponseDTO = UserAdapter.toDTO(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        UserResponseDTO userResponseDTO = UserAdapter.toDTO(user);
        return ResponseEntity.ok(userResponseDTO);
    }

    @GetMapping
    public ResponseEntity<UserResponseDTO> getUserByName(@RequestParam String name){
        User user = userService.getUserByName(name);
        UserResponseDTO userResponseDTO = UserAdapter.toDTO(user);
        return ResponseEntity.ok(userResponseDTO);
    }
}
