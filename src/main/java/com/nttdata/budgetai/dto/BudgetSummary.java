package com.nttdata.budgetai.dto;

import java.math.BigDecimal;

public record BudgetSummary(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {}
