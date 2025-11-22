package com.example.WalletAppReal.repository;

import com.example.WalletAppReal.models.Transaction;
import com.example.WalletAppReal.models.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    //let's say if want all the transaction of a particular wallet
    List<Transaction> findByFromUserId(Long fromUserId);
    // all the credit transaction
    List<Transaction> findByToUserId(Long toUserId);

    //find a transaction by status
    List<Transaction> findByStatus(TransactionStatus transactionStatus);

    //find by sagaInstanceId
    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);
}
