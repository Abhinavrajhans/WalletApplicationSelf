package com.example.WalletAppReal.services;

import com.example.WalletAppReal.adapters.UserAdapter;
import com.example.WalletAppReal.dto.UserRequestDTO;
import com.example.WalletAppReal.models.User;
import com.example.WalletAppReal.repostiory.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(UserRequestDTO userRequestDTO) {
        log.info("Creating user {}", userRequestDTO.getName());
        User user = UserAdapter.toEntity(userRequestDTO);
        user=userRepository.save(user);
        log.info("Created user {} with name {}", user.getId(),user.getName());
        return user;
    }

    public User getUserById(Long id) {
        log.info("Retrieving user {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User not found"));
        log.info("Retrieved user {} with name {}", user.getId(),user.getName());
        return user;
    }

    public User getUserByName(String name)
    {
        log.info("Retrieving user {}", name);
        User user = userRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("User not found"));
        log.info("Retrieved user {} with name {}", user.getId(),user.getName());
        return user;
    }
}
