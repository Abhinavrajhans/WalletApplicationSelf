package com.example.WalletAppReal.services.saga.steps;

import com.example.WalletAppReal.services.saga.ISagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {

    private final Map<String , ISagaStep> sagaStepMap;

    public static enum SagaStepType{
        DEBIT_SOURCE_WALLET_STEP,
        CREDIT_DESTINATION_WALLET_STEP,
        UPDATE_TRANSACTION_STATUS_STEP
    }

    public ISagaStep getSagaStep(String stepName) { return sagaStepMap.get(stepName);}

    public static List<SagaStepType> TransferMoneySagaSteps = List.of(
            SagaStepType.DEBIT_SOURCE_WALLET_STEP,
            SagaStepType.CREDIT_DESTINATION_WALLET_STEP,
            SagaStepType.UPDATE_TRANSACTION_STATUS_STEP
    );
}
