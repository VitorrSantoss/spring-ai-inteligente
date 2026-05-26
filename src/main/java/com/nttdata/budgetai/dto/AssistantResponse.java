package com.nttdata.budgetai.dto;

public record AssistantResponse(
        String userMessage,    // O que o usuário disse (texto ou transcrito do áudio)
        String assistantReply  // Resposta da IA em texto
) {}
