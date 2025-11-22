package com.example.WalletAppReal.services;


import com.example.WalletAppReal.adapters.WalletAdapter;
import com.example.WalletAppReal.dto.CreditWalletRequestDTO;
import com.example.WalletAppReal.dto.DebitWalletRequestDTO;
import com.example.WalletAppReal.dto.WalletRequestDTO;
import com.example.WalletAppReal.models.Wallet;
import com.example.WalletAppReal.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet createWallet(WalletRequestDTO walletRequestDTO){
        log.info("Creating Wallet for user {}",walletRequestDTO.getUserId());
        Wallet wallet = WalletAdapter.toEntity(walletRequestDTO);
        wallet = walletRepository.save(wallet);
        log.info("Wallet created for user {} with id {}",wallet.getUserId(),wallet.getId());
        return wallet;
    }

    public Wallet getWalletByUserId(Long userId) {
        log.info("Getting wallet for user id {}", userId);
        return walletRepository.findByUserId(userId)
                .orElseThrow(()-> new RuntimeException("Wallet for user not found"));
    }

    @Transactional
    public void debit(Long userId, DebitWalletRequestDTO debitWalletRequestDTO){
        log.info("Debiting Amount {} for user {}", debitWalletRequestDTO.getAmount(), userId);

        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(()-> new RuntimeException("Wallet not found"));

        wallet.debit(debitWalletRequestDTO.getAmount());
        walletRepository.save(wallet);
        log.info("Debit successful for user {} with wallet id {}", userId, wallet.getId());
    }

    @Transactional
    public void credit(Long userId, CreditWalletRequestDTO creditWalletRequestDTO){
        log.info("Credit Amount {} for user {}", creditWalletRequestDTO.getAmount(), userId);

        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(()-> new RuntimeException("Wallet not found"));

        wallet.credit(creditWalletRequestDTO.getAmount());
        walletRepository.save(wallet);
        log.info("Credit successful for user {} with wallet id {}", userId, wallet.getId());
    }

    public BigDecimal getWalletBalanceByUserId(Long userId){
        log.info("Getting wallet balance for user id {}", userId);
        BigDecimal balance = getWalletByUserId(userId).getBalance();
        log.info("Wallet balance successfully fetched for user id {}", userId);
        return balance;
    }
}