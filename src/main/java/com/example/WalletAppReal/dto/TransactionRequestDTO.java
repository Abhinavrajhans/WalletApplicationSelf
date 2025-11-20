package com.example.WalletAppReal.dto;

import com.example.WalletAppReal.models.TransactionStatus;
import com.example.WalletAppReal.models.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequestDTO {

    @NotNull(message="fromWalletId is required")
    private Long fromWalletId;

    @NotNull(message="toWalletId is required")
    private Long toWalletId;

    @NotNull(message="amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message="fromWalletId is required")
    private String description;

}
