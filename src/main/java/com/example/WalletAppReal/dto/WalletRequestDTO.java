package com.example.WalletAppReal.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletRequestDTO {

    @NotNull(message="userId is required")
    private Long userId;

}
