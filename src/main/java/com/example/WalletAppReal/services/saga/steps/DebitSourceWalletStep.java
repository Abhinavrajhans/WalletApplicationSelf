package com.example.WalletAppReal.services.saga.steps;

import com.example.WalletAppReal.models.Wallet;
import com.example.WalletAppReal.repository.WalletRepository;
import com.example.WalletAppReal.services.saga.ISagaStep;
import com.example.WalletAppReal.services.saga.SagaContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.WalletAppReal.services.saga.steps.SagaStepFactory.SagaStepType;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class DebitSourceWalletStep implements ISagaStep {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext sagaContext) {
        Long fromUserId = sagaContext.getLong("fromUserId");
        BigDecimal amount = sagaContext.getBigDecimal("amount");
        log.info("Debiting source wallet for user {} with amount {}", fromUserId, amount);
        Wallet wallet = walletRepository.findByUserIdWithLock(fromUserId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user"));
        log.info("Wallet Fetched with balance {}", wallet.getBalance());
        sagaContext.put("originalSourceWalletBalance",wallet.getBalance());
        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with balance {}", wallet.getBalance());
        sagaContext.put("sourceWalletBalanceAfterDebit",wallet.getBalance());
        log.info("Debit source Wallet step executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext sagaContext) {
        Long fromUserId = sagaContext.getLong("fromUserId");
        BigDecimal amount = sagaContext.getBigDecimal("amount");
        log.info("Compensating Debit source wallet for user {} with amount {}", fromUserId, amount);

        Wallet wallet = walletRepository.findByUserIdWithLock(fromUserId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user"));

        log.info("Wallet Fetched with balance {}", wallet.getBalance());
        sagaContext.put("sourceWalletBalanceBeforeCreditCompensation",wallet.getBalance());

        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with balance {}", wallet.getBalance());
        sagaContext.put("sourceWalletBalanceAfterCreditCompensation",wallet.getBalance());
        log.info("Credit compensation of source wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString();
    }
}
