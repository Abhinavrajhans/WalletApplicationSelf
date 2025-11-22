package com.example.WalletAppReal.services;

import com.example.WalletAppReal.adapters.TransactionAdapter;
import com.example.WalletAppReal.dto.TransactionRequestDTO;
import com.example.WalletAppReal.models.Transaction;
import com.example.WalletAppReal.models.TransactionStatus;
import com.example.WalletAppReal.repository.TransactionRepository;
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
    public Transaction createTransaction(TransactionRequestDTO transactionRequestDTO) {
        log.info("Creating Transaction from user {}  to user {} with amount {} and description {}",
                transactionRequestDTO.getFromUserId(),
                transactionRequestDTO.getToUserId(),
                transactionRequestDTO.getAmount(),
                transactionRequestDTO.getDescription()
        );

        Transaction transaction = TransactionAdapter.toEntity(transactionRequestDTO);
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with Id {}", savedTransaction.getId());
        return savedTransaction;
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Transaction not found"));
    }

    public List<Transaction> getTransactionByFromUserId(Long fromUserId) {
        return transactionRepository.findByFromUserId(fromUserId);
    }

    public List<Transaction> getTransactionByToUserId(Long toUserId) {
        return transactionRepository.findByToUserId(toUserId);
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

}
