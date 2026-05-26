package com.nttdata.budgetai.controller;

import com.nttdata.budgetai.dto.BudgetSummary;
import com.nttdata.budgetai.dto.TransactionRequest;
import com.nttdata.budgetai.dto.TransactionResponse;
import com.nttdata.budgetai.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST para o gerenciamento manual de transações.
 * (CRUD tradicional; o controle por voz fica no AssistantController.)
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Gerenciamento manual de transações financeiras")
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    @Operation(summary = "Cria uma nova transação (receita ou despesa)")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/transactions/" + created.getId()))
                .body(TransactionResponse.from(created));
    }

    @GetMapping
    @Operation(summary = "Lista todas as transações")
    public List<TransactionResponse> list() {
        return service.findAll().stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma transação por ID")
    public TransactionResponse getById(@PathVariable Long id) {
        return TransactionResponse.from(service.findById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma transação")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Retorna o resumo do orçamento (receitas, despesas e saldo)")
    public BudgetSummary summary() {
        return service.getSummary();
    }
}
