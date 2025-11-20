package com.example.WalletAppReal.services.saga.steps;

import com.example.WalletAppReal.models.SagaInstance;
import com.example.WalletAppReal.services.saga.SagaContext;

public interface ISagaOrchestrator {

    Long startSaga(SagaContext sagaContext);

    boolean executeStep(Long sagaInstanceId,String stepName);

    boolean  compensateStep(Long sagaInstanceId,String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void compensateSaga(Long sagaInstanceId);

    void failSaga(Long sagaInstanceId);

    void completeSaga(Long sagaInstanceId);
}
