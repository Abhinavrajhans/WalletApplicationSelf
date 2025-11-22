package com.example.WalletAppReal.repository;

import com.example.WalletAppReal.models.SagaStep;
import com.example.WalletAppReal.models.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {

    List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);

    List<SagaStep> findBySagaInstanceIdAndStatus(Long sagaInstanceId, StepStatus status);

    Optional<SagaStep> findBySagaInstanceIdAndStepNameAndStatus(Long sagaInstanceId, String stepName, StepStatus status);

    //make sure when u will execute the query in parallel timestamp will be identical so the order u have to maintain some
    // other way then sorting via timestamp will not work.
    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status = com.example.WalletAppReal.models.StepStatus.COMPLETED ORDER BY s.createdAt DESC")
    List<SagaStep> findCompletedStepsBySagaInstanceId(Long sagaInstanceId);
}
