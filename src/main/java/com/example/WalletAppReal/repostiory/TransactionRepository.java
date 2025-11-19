package com.example.WalletAppReal.repostiory;

import com.example.WalletAppReal.models.Transaction;
import com.example.WalletAppReal.models.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    //let's say if want all the transaction of a particular wallet
    List<Transaction> findByFromWalletId(Long fromWalletId);
    // all the credit transaction
    List<Transaction> findByToWalletId(Long toWalletId);
    // all the transaction
    @Query("SELECT t FROM Transaction t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    List<Transaction> findByWalletId(@Param("walletId") Long walletId);

    //find a transaction by status
    List<Transaction> findByStatus(TransactionStatus transactionStatus);

    //find by sagaInstanceId
    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);
}
