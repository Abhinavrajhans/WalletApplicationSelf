package com.example.WalletAppReal.adapters;

import com.example.WalletAppReal.dto.UserRequestDTO;
import com.example.WalletAppReal.dto.UserResponseDTO;
import com.example.WalletAppReal.models.User;

public class UserAdapter {

    public static User toEntity(UserRequestDTO userRequestDTO)
    {
        return User.builder()
                .name(userRequestDTO.getName())
                .email(userRequestDTO.getEmail())
                .build();
    }

    public static UserResponseDTO toDTO(User user)
    {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
