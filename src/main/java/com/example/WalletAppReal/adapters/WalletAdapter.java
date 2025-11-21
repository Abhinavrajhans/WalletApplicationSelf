package com.example.WalletAppReal.adapters;

import com.example.WalletAppReal.dto.WalletRequestDTO;
import com.example.WalletAppReal.dto.WalletResponseDTO;
import com.example.WalletAppReal.models.Wallet;

public class WalletAdapter {

    public static Wallet toEntity(WalletRequestDTO walletRequestDTO)
    {
        return Wallet.builder()
                .userId(walletRequestDTO.getUserId())
                .isActive(true)
                .build();
    }

    public static WalletResponseDTO toDTO(Wallet wallet)
    {
        return WalletResponseDTO.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .isActive(wallet.getIsActive())
                .balance(wallet.getBalance())
                .createdAt(wallet.getCreatedAt())
                .build();
    }
}
