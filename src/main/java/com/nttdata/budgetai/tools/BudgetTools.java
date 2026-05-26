package com.nttdata.budgetai.tools;

import com.nttdata.budgetai.dto.BudgetSummary;
import com.nttdata.budgetai.dto.TransactionRequest;
import com.nttdata.budgetai.model.Transaction;
import com.nttdata.budgetai.model.TransactionType;
import com.nttdata.budgetai.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Tool Calling (Spring AI 1.0 GA).
 *
 * Cada método anotado com {@code @Tool} é exposto automaticamente ao LLM.
 * O Spring AI gera o JSON Schema dos parâmetros (usando {@code @ToolParam})
 * e o GPT decide sozinho qual método chamar a partir da intenção do usuário.
 *
 * Para ativar, basta passar esta bean ao ChatClient via {@code .tools(budgetTools)}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetTools {

    private final TransactionService service;

    // ---------- TOOL 1: registrar transação ----------
    @Tool(description = """
            Registra uma nova transação financeira (receita ou despesa) no orçamento do usuário.
            Use type=INCOME para receitas/ganhos (recebi, ganhei, entrou) e type=EXPENSE
            para despesas/gastos (gastei, paguei, comprei).
            Sempre informe uma categoria como Alimentação, Transporte, Salário, Lazer,
            Saúde, Moradia, Educação, etc.
            """)
    public String addTransaction(
            @ToolParam(description = "Descrição curta da transação. Ex: 'almoço no restaurante'")
            String description,

            @ToolParam(description = "Valor monetário em reais, sempre positivo. Ex: 45.90")
            BigDecimal amount,

            @ToolParam(description = "Tipo: INCOME para receita ou EXPENSE para despesa")
            TransactionType type,

            @ToolParam(description = "Categoria da transação. Ex: Alimentação, Transporte, Salário")
            String category
    ) {
        log.info("🤖 [TOOL] addTransaction: desc={}, amount={}, type={}, cat={}",
                description, amount, type, category);
        Transaction created = service.create(
                new TransactionRequest(description, amount, type, category)
        );
        return String.format(
                "Transação id=%d registrada com sucesso: %s de R$ %.2f (%s/%s)",
                created.getId(), created.getDescription(), created.getAmount(),
                created.getType(), created.getCategory()
        );
    }

    // ---------- TOOL 2: resumo do orçamento ----------
    @Tool(description = """
            Retorna o resumo financeiro do orçamento: total de receitas,
            total de despesas e saldo atual. Use sempre que o usuário pedir
            informações sobre saldo, balanço, orçamento ou situação financeira geral.
            """)
    public BudgetSummary getBudgetSummary() {
        log.info("🤖 [TOOL] getBudgetSummary");
        return service.getSummary();
    }

    // ---------- TOOL 3: total por categoria ----------
    @Tool(description = """
            Retorna o total gasto ou recebido em uma categoria específica
            (ex: Alimentação, Transporte, Salário). Informe a categoria e o tipo
            (INCOME para receitas ou EXPENSE para despesas).
            """)
    public String getCategoryTotal(
            @ToolParam(description = "Nome da categoria. Ex: Alimentação")
            String category,

            @ToolParam(description = "Tipo: INCOME (receitas) ou EXPENSE (despesas)")
            TransactionType type
    ) {
        log.info("🤖 [TOOL] getCategoryTotal: categoria={}, tipo={}", category, type);
        BigDecimal total = service.sumByCategoryAndType(category, type);
        return String.format("Total %s em '%s': R$ %.2f", type, category, total);
    }
}
