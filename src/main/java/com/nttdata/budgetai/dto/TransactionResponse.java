package com.nttdata.budgetai.dto;

import com.nttdata.budgetai.model.Transaction;
import com.nttdata.budgetai.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getDescription(),
                t.getAmount(),
                t.getType(),
                t.getCategory(),
                t.getCreatedAt()
        );
    }
}
