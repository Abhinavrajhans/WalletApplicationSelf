package com.example.WalletAppReal.controller;


import com.example.WalletAppReal.dto.TransactionRequestDTO;
import com.example.WalletAppReal.models.Transaction;
import com.example.WalletAppReal.services.TransactionService;
import com.example.WalletAppReal.services.TransferSagaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transaction")
@Slf4j
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransferSagaService transferSagaService;

    @PostMapping
    public ResponseEntity<TransactionRequestDTO> createTransction(@Valid @RequestBody TransactionRequestDTO transactionRequestDTO) {
        log.info("Creating transaction {}", transactionRequestDTO);
        Long sagaInstanceId = transferSagaService.initiateTransfer(transactionRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionRequestDTO);
    }
}
