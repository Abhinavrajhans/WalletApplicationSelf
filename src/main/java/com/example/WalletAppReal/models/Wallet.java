package com.example.WalletAppReal.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="wallet")
public class Wallet extends BaseModel{

    @Column(name="user_id",nullable=false,unique=true)
    private Long userId;

    @Column(name="is_active",nullable=false)
    private Boolean isactive;

    @Column(name="balance",nullable=false)
    @Builder.Default
    private BigDecimal balance=BigDecimal.ZERO;

    public boolean hasSufficientBalance(BigDecimal amount){return this.balance.compareTo(amount)>=0;}

    public void debit(BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if(!hasSufficientBalance(amount)){
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance=this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        this.balance=this.balance.add(amount);
    }

}
