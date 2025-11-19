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

    @Column(name="from_wallet_id", nullable=false)
    private Long fromWalletId;

    @Column(name="to_wallet_id",nullable=false)
    private Long toWalletId;

    @Column(name="amount",nullable=false)
    private BigDecimal amount;

    @Enumerated
    @Column(name="status",nullable=false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name="transaction_type" , nullable=false)
    @Builder.Default
    private TransactionType type=TransactionType.TRANSFER;

    @Column(name="description",nullable=false)
    private String description;

    @Column(name="saga_instance_id")
    private Long sagaInstanceId;


}
