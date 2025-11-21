package com.example.WalletAppReal.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestDTO {

    @NotBlank(message ="name is required")
    @Size(min=3 , max=100 , message="name character must be between 3 and 100")
    private String name;

    @NotBlank(message ="email is required")
    @Email(message="email is invalid")
    private String email;
}
