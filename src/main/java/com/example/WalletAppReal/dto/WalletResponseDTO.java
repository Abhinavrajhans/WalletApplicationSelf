package com.example.WalletAppReal.dto;

import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletResponseDTO {
    private Long id;
    private Long userId;
    private Boolean isActive;
    private BigDecimal balance;
    private Date createdAt;
}
