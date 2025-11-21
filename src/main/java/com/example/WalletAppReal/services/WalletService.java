package com.example.WalletAppReal.services;


import com.example.WalletAppReal.adapters.WalletAdapter;
import com.example.WalletAppReal.dto.CreditWalletRequestDTO;
import com.example.WalletAppReal.dto.DebitWalletRequestDTO;
import com.example.WalletAppReal.dto.WalletRequestDTO;
import com.example.WalletAppReal.dto.WalletResponseDTO;
import com.example.WalletAppReal.models.Wallet;
import com.example.WalletAppReal.repostiory.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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

    public Wallet getWalletById(Long id){
        log.info("Getting Wallet for wallet id {}",id);
        return  walletRepository.findById(id).orElseThrow(()-> new RuntimeException("Wallet not found"));
    }

    public Wallet getWalletByUserId(Long userId) {
        log.info("Getting wallet for user id {}", userId);
        return walletRepository.findByUserId(userId).orElseThrow(()-> new RuntimeException("wallet for user not Found"));
    }

    @Transactional
    public void debit(Long userId , DebitWalletRequestDTO debitWalletRequestDTO){
        log.info("Debiting Amount {} for user {}",debitWalletRequestDTO.getAmount(),userId);
        Wallet wallet=getWalletById(userId);
        wallet.debit(debitWalletRequestDTO.getAmount());
        walletRepository.save(wallet);
        log.info("Debit successful for user {} with id {}",userId,wallet.getId());
    }

    @Transactional
    public void credit(Long userId , CreditWalletRequestDTO creditWalletRequestDTO){
        log.info("Credit Amount {} for user {}",creditWalletRequestDTO.getAmount(),userId);
        Wallet wallet=getWalletById(userId);
        wallet.credit(creditWalletRequestDTO.getAmount());
        walletRepository.save(wallet);
        log.info("Credit successful for user {} with id {}",userId,wallet.getId());
    }

    public BigDecimal getWalletBalance(Long walletId){
        log.info("Getting wallet balance for wallet id {}",walletId);
        BigDecimal balance= getWalletById(walletId).getBalance();
        log.info("Wallet balance successfully fetched for wallet id {}",walletId);
        return balance;
    }
}
