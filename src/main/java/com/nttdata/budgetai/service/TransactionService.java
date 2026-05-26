package com.nttdata.budgetai.service;

import com.nttdata.budgetai.dto.BudgetSummary;
import com.nttdata.budgetai.dto.TransactionRequest;
import com.nttdata.budgetai.model.Transaction;
import com.nttdata.budgetai.model.TransactionType;
import com.nttdata.budgetai.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository repository;

    public Transaction create(TransactionRequest request) {
        Transaction t = Transaction.builder()
                .description(request.description())
                .amount(request.amount())
                .type(request.type())
                .category(request.category() != null ? request.category() : "Geral")
                .build();
        Transaction saved = repository.save(t);
        log.info("Transação criada: id={}, tipo={}, valor={}",
                saved.getId(), saved.getType(), saved.getAmount());
        return saved;
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }

    public Transaction findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada: " + id));
    }

    public void delete(Long id) {
        repository.deleteById(id);
        log.info("Transação removida: id={}", id);
    }

    public BudgetSummary getSummary() {
        BigDecimal income = repository.sumByType(TransactionType.INCOME);
        BigDecimal expense = repository.sumByType(TransactionType.EXPENSE);
        return new BudgetSummary(income, expense, income.subtract(expense));
    }

    public BigDecimal sumByCategoryAndType(String category, TransactionType type) {
        return repository.sumByTypeAndCategory(type, category);
    }
}
