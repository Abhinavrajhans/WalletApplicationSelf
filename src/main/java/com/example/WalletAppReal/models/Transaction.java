package com.example.WalletAppReal.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="transaction")
public class Transaction extends BaseModel{

    @Column(name="from_user_id", nullable=false, updatable=false)
    private Long fromUserId;

    @Column(name="to_user_id", nullable=false, updatable=false)
    private Long toUserId;

    @Column(name="amount", nullable=false, updatable=false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name="transaction_type", nullable=false, updatable=false)
    @Builder.Default
    private TransactionType type = TransactionType.TRANSFER;

    @Column(name="description", nullable=false, updatable=false)
    private String description;

    @Column(name="saga_instance_id")
    private Long sagaInstanceId;
}