package com.example.WalletAppReal.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name="user")
public class User extends BaseModel{

    @NotBlank(message ="name is required")
    @Size(min=3 , max=100 , message="name character must be between 3 and 100")
    private String name;

    @NotBlank(message ="email is required")
    @Email(message="email is invalid")
    private String email;
}
