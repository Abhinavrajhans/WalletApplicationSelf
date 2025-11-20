package com.example.WalletAppReal.dto;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletResponseDTO {
    private Long userId;
    private Boolean isActive;
}
