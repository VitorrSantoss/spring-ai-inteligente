package com.nttdata.budgetai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Budget AI API - Bootcamp NTT DATA: Backend Java com Spring AI
 *
 * API Inteligente com Reconhecimento de Fala usando Spring Boot 3 + Spring AI.
 * Permite controle de orçamento por voz: o usuário envia áudio descrevendo
 * gastos/receitas, o Whisper transcreve, o GPT executa as funções (Tool Calling),
 * e o TTS responde com voz natural.
 */
@SpringBootApplication
public class BudgetAiApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetAiApiApplication.class, args);
    }
}
