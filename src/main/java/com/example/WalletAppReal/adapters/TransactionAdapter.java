package com.example.WalletAppReal.adapters;

import com.example.WalletAppReal.dto.TransactionRequestDTO;
import com.example.WalletAppReal.dto.TransactionResponseDTO;
import com.example.WalletAppReal.models.Transaction;
import com.example.WalletAppReal.models.TransactionStatus;
import com.example.WalletAppReal.models.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;

import java.math.BigDecimal;

public class TransactionAdapter {

    public static Transaction toEntity(TransactionRequestDTO transactionRequestDTO)
    {
        return Transaction.builder()
                .toUserId(transactionRequestDTO.getToUserId())
                .fromUserId(transactionRequestDTO.getFromUserId())
                .amount(transactionRequestDTO.getAmount())
                .description(transactionRequestDTO.getDescription())
                .build();
    }
    public static TransactionResponseDTO  toDTO(Long sagaInstanceId)
    {
        return TransactionResponseDTO.builder()
                .sagaInstanceId(sagaInstanceId)
                .build();
    }
}
