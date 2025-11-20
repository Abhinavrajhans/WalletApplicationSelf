package com.example.WalletAppReal.dto;


import com.example.WalletAppReal.models.TransactionStatus;
import com.example.WalletAppReal.models.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponseDTO {
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private TransactionStatus status;
    private TransactionType type;
    private String description;
    private Long sagaInstanceId;
}
