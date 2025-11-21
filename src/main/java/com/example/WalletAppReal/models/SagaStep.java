package com.example.WalletAppReal.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="saga_step")
@Entity
public class SagaStep extends BaseModel{

    @Column(name="saga_instance_id",nullable=false)
    private Long sagaInstanceId;

    @Column(name="step_name" , nullable=false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private StepStatus status;

    @Column(name="error_message")
    private String errorMessage;

    @Column(name="step_data" , columnDefinition = "json")
    private String stepData;

    public void markAsFailed() {
        this.status = StepStatus.FAILED;
    }

    public void markAsPending() {
        this.status = StepStatus.PENDING;
    }

    public void markAsRunning() {
        this.status = StepStatus.RUNNING;
    }

    public void markAsCompleted(){
        this.status = StepStatus.COMPLETED;
    }

    public void marksAsCompensating(){
        this.status = StepStatus.COMPENSATING;
    }

    public void markAsCompensated() {
        this.status = StepStatus.COMPENSATED;
    }

}
