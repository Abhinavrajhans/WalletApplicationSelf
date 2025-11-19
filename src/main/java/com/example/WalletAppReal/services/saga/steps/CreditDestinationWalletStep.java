package com.example.WalletAppReal.services.saga.steps;

import com.example.WalletAppReal.models.Wallet;
import com.example.WalletAppReal.repostiory.WalletRepository;
import com.example.WalletAppReal.services.saga.ISagaStep;
import com.example.WalletAppReal.services.saga.SagaContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditDestinationWalletStep implements ISagaStep {


    private final WalletRepository walletRepository;


    @Override
    @Transactional
    public boolean execute(SagaContext sagaContext) {
        // Step 1: Get the destination wallet id from the context
        Long toWalletId = sagaContext.getLong("toWalletId");
        BigDecimal amount = sagaContext.getBigDecimal("amount");
        log.info("Crediting destination wallet {} with amount {}", toWalletId, amount);

        // Step 2: Fetch the destination wallet from the database with a lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + toWalletId));
        log.info("Wallet fetched with balance {}", wallet.getBalance());
        sagaContext.put("OriginalDestinationWalletBalance", wallet.getBalance());

        // Step 3: Credit the destination wallet
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet credited with balance {}", wallet.getBalance());
        log.info("Wallet saved in the db with balance {}", wallet.getBalance());
        sagaContext.put("DestinationWalletBalanceAfterCredit",wallet.getBalance());
        log.info("Credit Destination wallet step executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext sagaContext) {
        // Step 1: Get the destination wallet id from the context
        Long toWalletId = sagaContext.getLong("toWalletId");
        BigDecimal amount = sagaContext.getBigDecimal("amount");
        log.info("Compensating credit of destination wallet {} with amount {}", toWalletId, amount);

        // Step 2: Fetch the destination wallet from the database with a lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                        .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + toWalletId));
        sagaContext.put("DestinationWalletBalanceBeforeCreditCompensation",wallet.getBalance());
        log.info("Wallet fetched with balance {}", wallet.getBalance());

        // Step 3: Debit the destination wallet
        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with balance {}", wallet.getBalance());
        sagaContext.put("DestinationWalletBalanceAfterCreditCompensation",wallet.getBalance());

        log.info("Credit compensation of destination wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return "CreditDestinationWalletStep";
    }
}
