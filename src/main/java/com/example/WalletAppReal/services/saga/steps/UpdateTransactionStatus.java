package com.example.WalletAppReal.services.saga.steps;

import com.example.WalletAppReal.models.Transaction;
import com.example.WalletAppReal.models.TransactionStatus;
import com.example.WalletAppReal.repository.TransactionRepository;
import com.example.WalletAppReal.services.saga.ISagaStep;
import com.example.WalletAppReal.services.saga.SagaContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.WalletAppReal.services.saga.steps.SagaStepFactory.SagaStepType;


@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateTransactionStatus implements ISagaStep {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean execute(SagaContext sagaContext) {
        Long transactionId = sagaContext.getLong("transactionId");
        log.info("Updating transaction status for transactionId={}", transactionId);
        Transaction transaction=transactionRepository.findById(transactionId).
                orElseThrow(()-> new RuntimeException("Transaction not found"));
        log.info("Transaction fetched for transaction {}", transactionId);
        sagaContext.put("originalTransactionStatus", transaction.getStatus());
        setTransactionStatus(transaction, sagaContext, "destinationTransactionStatus");
        transactionRepository.save(transaction);
        log.info("Transaction status updated for transaction {}", transactionId);
        sagaContext.put("transactionStatusAfterUpdate", transaction.getStatus());
        log.info("Update transaction status step exectuted successfully");
        return true;
    }

    @Override
    public boolean compensate(SagaContext sagaContext) {
        Long transactionId = sagaContext.getLong("transactionId");
        log.info("Compensating transaction status for transactionId={}", transactionId);
        Transaction transaction =  transactionRepository.findById(transactionId).
                orElseThrow(()-> new RuntimeException("Transaction not found"));

        log.info("Transaction fetched for transaction {}", transactionId);
        sagaContext.put("originalTransactionStatusBeforeCompensation", transaction.getStatus());
        setTransactionStatus(transaction, sagaContext, "originalTransactionStatus");
        transactionRepository.save(transaction);
        log.info("Transaction status compensated for transaction {}", transactionId);
        sagaContext.put("transactionStatusAfterCompensation", transaction.getStatus());
        log.info("Compensate transaction status step exectuted successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString();
    }

    private void setTransactionStatus(Transaction transaction, SagaContext sagaContext, String key) {
        String statusStr = sagaContext.getString(key);
        if (statusStr == null) {
            throw new IllegalStateException("Transaction status not found in context for key: " + key);
        }
        TransactionStatus status = TransactionStatus.valueOf(statusStr);
        transaction.setStatus(status);
    }
}
