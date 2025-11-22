package com.example.WalletAppReal.services.saga.steps;

import com.example.WalletAppReal.models.SagaInstance;
import com.example.WalletAppReal.models.SagaStatus;
import com.example.WalletAppReal.models.SagaStep;
import com.example.WalletAppReal.models.StepStatus;
import com.example.WalletAppReal.repository.SagaInstanceRepository;
import com.example.WalletAppReal.repository.SagaStepRepository;
import com.example.WalletAppReal.services.saga.ISagaStep;
import com.example.WalletAppReal.services.saga.SagaContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SagaOrchestratorImpl implements ISagaOrchestrator {

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepFactory sagaStepFactory;
    private final SagaStepRepository sagaStepRepository;

    @Override
    @Transactional
    public Long startSaga(SagaContext sagaContext) {
        try {
            String contextJson = objectMapper.writeValueAsString(sagaContext);
            SagaInstance sagaInstance = SagaInstance.builder()
                    .context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();
            sagaInstance = sagaInstanceRepository.save(sagaInstance);
            log.info("Saga instance created: {} and marked status as {}", sagaInstance.getId(), SagaStatus.STARTED);
            return sagaInstance.getId();
        } catch (Exception e) {
            log.error("Error starting saga", e);
            throw new RuntimeException("Error starting saga", e);
        }
    }

    @Override
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));

        ISagaStep step = sagaStepFactory.getSagaStep(stepName);
        if (step == null) {
            log.error("Saga step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found for step name " + stepName);
        }

        SagaStep sagaStepDB = sagaStepRepository
                .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING)
                .orElse(SagaStep.builder()
                        .sagaInstanceId(sagaInstanceId)
                        .stepName(stepName)
                        .status(StepStatus.PENDING)
                        .build()
                );

        if (sagaStepDB.getId() == null) {
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }

        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.markAsRunning();
            sagaStepDB = sagaStepRepository.save(sagaStepDB);

            boolean success = step.execute(sagaContext);

            if (success) {
                sagaStepDB.markAsCompleted();
                sagaStepRepository.save(sagaStepDB);

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.markAsRunning();

                // ✅ CRITICAL: Save updated context
                String updatedContext = objectMapper.writeValueAsString(sagaContext);
                sagaInstance.setContext(updatedContext);

                sagaInstanceRepository.save(sagaInstance);
                log.info("Saga step {} executed successfully", stepName);
                return true;
            } else {
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB);
                log.error("Saga step {} execution failed", stepName);
                return false;
            }
        } catch (Exception e) {
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);
            log.error("Saga step {} execution failed", stepName, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("SagaInstance not found"));

        ISagaStep step = sagaStepFactory.getSagaStep(stepName);
        if (step == null) {
            log.error("Saga step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found for step name " + stepName);
        }

        SagaStep sagaStepDB = sagaStepRepository
                .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.COMPLETED)
                .orElse(null);

        if (sagaStepDB == null) {
            log.info("Step {} not found in the db for saga instance {}, so it is already compensated or was never executed",
                    stepName, sagaInstanceId);
            return true;
        }

        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.marksAsCompensating();
            sagaStepRepository.save(sagaStepDB);

            boolean success = step.compensate(sagaContext);

            if (success) {
                // ✅ Save updated context after compensation
                String updatedContext = objectMapper.writeValueAsString(sagaContext);
                sagaInstance.setContext(updatedContext);
                sagaInstanceRepository.save(sagaInstance);

                sagaStepDB.markAsCompensated();
                sagaStepRepository.save(sagaStepDB);
                log.info("Saga step {} compensated successfully", stepName);
                return true;
            } else {
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB);
                log.error("Saga step {} compensation failed", stepName);
                return false;
            }
        } catch (Exception e) {
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);
            log.error("Saga step {} compensation failed", stepName, e);
            return false;
        }
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));
    }

    @Override
    @Transactional
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));

        sagaInstance.markAsCompensating();
        sagaInstanceRepository.save(sagaInstance);

        List<SagaStep> completedSagaSteps = sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);

        boolean allCompensated = true;
        for (SagaStep sagaStep : completedSagaSteps) {
            boolean compensated = this.compensateStep(sagaInstanceId, sagaStep.getStepName());
            if (!compensated) {
                allCompensated = false;
            }
        }

        if (allCompensated) {
            sagaInstance.markAsCompensated();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga {} compensated successfully", sagaInstanceId);
        } else {
            log.error("Saga {} compensation failed", sagaInstanceId);
        }
    }

    @Override
    @Transactional
    public void failSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga Instance Not Found"));
        sagaInstance.markAsFailed();
        sagaInstanceRepository.save(sagaInstance);

        compensateSaga(sagaInstanceId);

        log.info("Saga {} failed and compensation initiated", sagaInstanceId);
    }

    @Override
    @Transactional
    public void completeSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));
        sagaInstance.markAsCompleted();
        sagaInstanceRepository.save(sagaInstance);
        log.info("Saga {} completed successfully", sagaInstanceId);
    }
}