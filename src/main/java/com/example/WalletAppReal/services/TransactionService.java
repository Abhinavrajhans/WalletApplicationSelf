package com.example.WalletAppReal.services;

import com.example.WalletAppReal.dto.TransactionRequestDTO;
import com.example.WalletAppReal.models.Transaction;
import com.example.WalletAppReal.models.TransactionStatus;
import com.example.WalletAppReal.repostiory.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction(TransactionRequestDTO  transactionRequestDTO) {
        log.info("Creating Transaction from wallet {}  to wallet with amount {} and description {}",
                transactionRequestDTO.getFromWalletId(),
                transactionRequestDTO.getToWalletId(),
                transactionRequestDTO.getDescription()
        );

        Transaction transaction = Transaction.builder()
                .fromWalletId(transactionRequestDTO.getFromWalletId())
                .toWalletId(transactionRequestDTO.getToWalletId())
                .description(transactionRequestDTO.getDescription())
                .amount(transactionRequestDTO.getAmount())
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with Id {}", savedTransaction.getId());
        return savedTransaction;
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Transaction not found"));
    }
    public List<Transaction> getTransactionByWalletId(Long walletId) {
        return transactionRepository.findByWalletId(walletId);
    }

    public List<Transaction> getTransactionByFromWalletId(Long fromWalletId) {
        return transactionRepository.findByFromWalletId(fromWalletId);
    }

    public List<Transaction> getTransactionByToWalletId(Long toWalletId) {
        return transactionRepository.findByToWalletId(toWalletId);
    }

    public List<Transaction> getTransactionBySagaInstanceId(Long sagaInstanceId) {
        return transactionRepository.findBySagaInstanceId(sagaInstanceId);
    }

    public List<Transaction> getTransactionByStatus(TransactionStatus transactionStatus) {
        return transactionRepository.findByStatus(transactionStatus);
    }

    public void updateTransactionWithSagaInstanceId(Long transactionId , Long sagaInstanceId) {
        Transaction transaction = getTransactionById(transactionId);
        transaction.setSagaInstanceId(sagaInstanceId);
        transactionRepository.save(transaction);
        log.info("Transaction updated successfully with Id {}", transaction.getId());
    }

    public void updateTransactionStatus(Long transactionId, TransactionStatus transactionStatus) {
        Transaction transaction = getTransactionById(transactionId);
        transaction.setStatus(transactionStatus);
        transactionRepository.save(transaction);
        log.info("Transaction Status updated successfully with Id {} with status {}", transaction.getId() ,  transactionStatus);
    }
}
