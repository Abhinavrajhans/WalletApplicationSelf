package com.example.WalletAppReal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DebitWalletRequestDTO {
    @NotNull(message="Amount is Required")
    private BigDecimal amount;
}
