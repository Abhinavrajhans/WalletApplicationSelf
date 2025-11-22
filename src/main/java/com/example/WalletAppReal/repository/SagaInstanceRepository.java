package com.example.WalletAppReal.repository;

import com.example.WalletAppReal.models.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {

}
