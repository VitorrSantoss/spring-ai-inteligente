package com.nttdata.budgetai.service;

import com.nttdata.budgetai.tools.BudgetTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

/**
 * AssistantService - Orquestrador principal do Spring AI 1.0 GA.
 *
 * Usa o ChatClient (camada fluente sobre o ChatModel) com:
 *  - System prompt: define a persona do assistente financeiro
 *  - MessageChatMemoryAdvisor: mantém contexto da conversa em janela deslizante
 *  - BudgetTools: 3 métodos @Tool que o LLM pode chamar automaticamente
 *
 * O LLM (gpt-4o-mini) decide AUTOMATICAMENTE qual tool chamar baseado
 * na intenção do usuário.
 */
@Service
@Slf4j
public class AssistantService {

    private static final String SYSTEM_PROMPT = """
        Você é o "Budget AI", um assistente financeiro pessoal amigável e objetivo,
        que responde sempre em português brasileiro.

        Suas responsabilidades:
        1. Registrar receitas e despesas quando o usuário informar gastos ou ganhos,
           usando a função addTransaction.
        2. Consultar o saldo e situação do orçamento com getBudgetSummary.
        3. Informar totais por categoria com getCategoryTotal.
        4. Categorizar automaticamente as transações (ex: Alimentação, Transporte,
           Salário, Lazer, Saúde, Moradia, Educação).
        5. Dar respostas curtas, claras e úteis (ideais para serem ouvidas em áudio).

        REGRAS IMPORTANTES:
        - Se o usuário disser "gastei", "paguei", "comprei" → use type=EXPENSE.
        - Se o usuário disser "recebi", "ganhei", "entrou" → use type=INCOME.
        - Sempre confirme em uma frase curta o que foi feito.
        - Valores em reais (R$). Se não houver moeda explícita, assuma reais.
        - Se a informação estiver incompleta, peça gentilmente o que falta.
        """;

    private final ChatClient chatClient;
    private final BudgetTools budgetTools;

    public AssistantService(ChatClient.Builder builder, BudgetTools budgetTools) {
        this.budgetTools = budgetTools;

        // Memória em janela deslizante (até 20 mensagens), em memória.
        // Em produção, troque InMemoryChatMemoryRepository por uma implementação
        // baseada em JDBC/Redis para persistir o histórico entre reinícios.
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * Processa uma mensagem (texto) e devolve a resposta do assistente.
     * O ChatClient resolve as tools automaticamente.
     */
    public String chat(String userMessage) {
        log.info("👤 Usuário: {}", userMessage);
        String reply = chatClient.prompt()
                .user(userMessage)
                .tools(budgetTools)   // expõe todos os métodos @Tool da bean
                .call()
                .content();
        log.info("🤖 Assistente: {}", reply);
        return reply;
    }
}
