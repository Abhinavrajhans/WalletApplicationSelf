package com.example.WalletAppReal.services.saga;

public interface ISagaStep {
    public boolean execute(SagaContext sagaContext);
    public boolean compensate(SagaContext sagaContext);
    public String getStepName();
}
