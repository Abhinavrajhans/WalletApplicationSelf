package com.example.WalletAppReal.services;

import com.example.WalletAppReal.dto.TransactionRequestDTO;
import com.example.WalletAppReal.models.Transaction;
import com.example.WalletAppReal.services.saga.SagaContext;
import com.example.WalletAppReal.services.saga.steps.ISagaOrchestrator;
import com.example.WalletAppReal.services.saga.steps.SagaStepFactory.SagaStepType;
import com.example.WalletAppReal.services.saga.steps.SagaStepFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSagaService {

    private final TransactionService transactionService;
    private final ISagaOrchestrator sagaOrchestrator;

    @Transactional
    public Long initiateTransfer(TransactionRequestDTO transactionRequestDTO) {

        log.info("Initiating transfer from user {} to user {} with amount {} and description {}",
                transactionRequestDTO.getFromUserId(),
                transactionRequestDTO.getToUserId(),
                transactionRequestDTO.getAmount(),
                transactionRequestDTO.getDescription()
        );

        Transaction transaction = transactionService.createTransaction(transactionRequestDTO);
        SagaContext sagaContext = SagaContext.builder()
                .data(Map.ofEntries(
                        Map.entry("transactionId", transaction.getId()),
                        Map.entry("fromUserId", transactionRequestDTO.getFromUserId()),
                        Map.entry("toUserId", transactionRequestDTO.getToUserId()),
                        Map.entry("amount", transactionRequestDTO.getAmount()),
                        Map.entry("description", transactionRequestDTO.getDescription()),
                        Map.entry("destinationTransactionStatus", "SUCCESS")
                ))
                .build();
        Long sagaInstanceId = sagaOrchestrator.startSaga(sagaContext);
        log.info("Saga Instance created with id {} ", sagaInstanceId);
        transactionService.updateTransactionWithSagaInstanceId(transaction.getId(), sagaInstanceId);
        executeTransferSaga(sagaInstanceId);
        return sagaInstanceId;
    }

    @Transactional
    public void executeTransferSaga(Long sagaInstanceId) {
        log.info("Executing transfer Saga Instance with id {} ", sagaInstanceId);
        try{
            for(SagaStepType step: SagaStepFactory.TransferMoneySagaSteps)
            {
                boolean success = sagaOrchestrator.executeStep(sagaInstanceId,step.toString());
                if(!success)
                {
                    log.error("Failed to execute step {}",step.toString());
                    sagaOrchestrator.failSaga(sagaInstanceId);
                    return;
                }
            }
            sagaOrchestrator.completeSaga(sagaInstanceId);
            log.info("Successfully executed transfer SagaInstance with Id {} ", sagaInstanceId);
        }
        catch(Exception e) {
            log.info("Failed to execute transfer saga with id {}",sagaInstanceId,e);
            sagaOrchestrator.failSaga(sagaInstanceId);
        }
    }
}
