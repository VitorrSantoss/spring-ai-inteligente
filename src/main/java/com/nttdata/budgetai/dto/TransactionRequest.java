package com.nttdata.budgetai.dto;

import com.nttdata.budgetai.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "Descrição é obrigatória")
        String description,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "Tipo é obrigatório (INCOME ou EXPENSE)")
        TransactionType type,

        String category
) {}
