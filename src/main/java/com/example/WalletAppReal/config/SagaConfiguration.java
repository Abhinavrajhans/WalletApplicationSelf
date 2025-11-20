package com.example.WalletAppReal.config;

import com.example.WalletAppReal.services.saga.ISagaStep;
import com.example.WalletAppReal.services.saga.steps.CreditDestinationWalletStep;
import com.example.WalletAppReal.services.saga.steps.DebitSourceWalletStep;
import com.example.WalletAppReal.services.saga.steps.UpdateTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SagaConfiguration {

    @Bean
    public Map<String, ISagaStep> sagaStepMap(
            DebitSourceWalletStep debitSourceWalletStep,
            CreditDestinationWalletStep creditDestinationWalletStep,
            UpdateTransactionStatus updateTransactionStatus
    ) {
        Map<String, ISagaStep> sagaStepMap = new HashMap<>();
        sagaStepMap.put(debitSourceWalletStep.getStepName(), debitSourceWalletStep);
        sagaStepMap.put(creditDestinationWalletStep.getStepName(), creditDestinationWalletStep);
        sagaStepMap.put(updateTransactionStatus.getStepName(), updateTransactionStatus);
        return sagaStepMap;
    }
}
