package com.example.WalletAppReal.controller;


import com.example.WalletAppReal.adapters.WalletAdapter;
import com.example.WalletAppReal.dto.CreditWalletRequestDTO;
import com.example.WalletAppReal.dto.DebitWalletRequestDTO;
import com.example.WalletAppReal.dto.WalletRequestDTO;
import com.example.WalletAppReal.dto.WalletResponseDTO;
import com.example.WalletAppReal.models.Wallet;
import com.example.WalletAppReal.services.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/wallet")
@Slf4j
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponseDTO> createWallet(@Valid @RequestBody WalletRequestDTO walletRequestDTO)
    {
        Wallet wallet =  walletService.createWallet(walletRequestDTO);
        WalletResponseDTO walletResponseDTO = WalletAdapter.toDTO(wallet);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletResponseDTO);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<WalletResponseDTO> getWalletById(@PathVariable Long id)
    {
        Wallet wallet = walletService.getWalletById(id);
        WalletResponseDTO walletResponseDTO = WalletAdapter.toDTO(wallet);
        return ResponseEntity.status(HttpStatus.OK).body(walletResponseDTO);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long id)
    {
        BigDecimal balance = walletService.getWalletBalance(id);
        return ResponseEntity.status(HttpStatus.OK).body(balance);
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<WalletResponseDTO> debitWallet(@PathVariable Long userId, @Valid @RequestBody DebitWalletRequestDTO  debitWalletRequestDTO)
    {
        walletService.debit(userId,debitWalletRequestDTO);
        Wallet wallet =  walletService.getWalletById(userId);
        WalletResponseDTO walletResponseDTO = WalletAdapter.toDTO(wallet);
        return ResponseEntity.status(HttpStatus.OK).body(walletResponseDTO);
    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<WalletResponseDTO> creditWallet(@PathVariable Long  userId, @Valid @RequestBody CreditWalletRequestDTO creditWalletRequestDTO)
    {
        walletService.credit(userId,creditWalletRequestDTO);
        Wallet wallet =  walletService.getWalletById(userId);
        WalletResponseDTO walletResponseDTO = WalletAdapter.toDTO(wallet);
        return ResponseEntity.status(HttpStatus.OK).body(walletResponseDTO);
    }

}
