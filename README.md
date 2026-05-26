# 🎙️ Budget AI API — Bootcamp NTT DATA: Backend Java com Spring AI

API inteligente de controle de orçamento pessoal com **reconhecimento de fala**,
**LLM com Tool Calling** e **resposta por voz**, construída em Spring Boot 3 +
Spring AI 1.0.

> Você fala → Whisper transcreve → GPT-4o-mini decide qual função chamar →
> os dados são persistidos no Postgres → o TTS responde em áudio.

---

## 🧩 Mapeamento dos módulos do Bootcamp

| # | Módulo | Onde está no projeto |
|---|---|---|
| 1 | **Spring AI: Setup e Integração com LLMs** | `pom.xml` + `application.yml` |
| 2 | **Explorando o ChatModel e Modelos de Linguagem** | `AssistantService` |
| 3 | **ChatClient: Fluência e Contexto no Spring AI** | `AssistantService` (system prompt + memória) |
| 4 | **Tool Calling: Executando Funções Reais com IA** | `tools/*` (3 funções registradas) |
| 5 | **Transcription API: Áudio → Texto** | `TranscriptionService` (Whisper) |
| 6 | **Speech API: Texto → Voz** | `SpeechService` (TTS-1, voz Nova) |
| 7 | **Integração do Assistente: Orquestrando o Fluxo de Budget** | `AssistantController` |
| 8 | **Persistência e Infraestrutura: Banco com Docker** | `docker-compose.yml` + JPA |
| 9 | **Exposição REST: TransactionController** | `TransactionController` |
| 10 | **Endpoint de Transcrição** | `POST /api/assistant/voice/transcribe` |
| 11 | **Roadmap e Auditoria: Evoluindo a API Inteligente** | Seção "Roadmap" abaixo |

---

## 🚀 Como rodar

### Pré-requisitos
- JDK 21+
- Maven 3.9+
- Docker / Docker Compose
- Chave da OpenAI (`OPENAI_API_KEY`)

### Passo a passo

```bash
# 1. Subir o PostgreSQL
docker-compose up -d

# 2. Exportar a chave da OpenAI
export OPENAI_API_KEY="sk-..."

# 3. Rodar a aplicação
mvn spring-boot:run
```

A API sobe em **http://localhost:8080**.
Swagger UI: **http://localhost:8080/swagger-ui.html**

---

## 📡 Endpoints

### Transações (CRUD manual)
| Método | URL | Descrição |
|---|---|---|
| `POST` | `/api/transactions` | Cria transação |
| `GET` | `/api/transactions` | Lista todas |
| `GET` | `/api/transactions/{id}` | Busca por id |
| `DELETE` | `/api/transactions/{id}` | Remove |
| `GET` | `/api/transactions/summary` | Resumo (receita, despesa, saldo) |

### Assistente Inteligente
| Método | URL | Descrição |
|---|---|---|
| `POST` | `/api/assistant/chat` | Conversa por texto (com tools) |
| `POST` | `/api/assistant/voice/transcribe` | Áudio → texto (Whisper) |
| `POST` | `/api/assistant/voice/speak` | Texto → áudio (TTS) |
| `POST` | `/api/assistant/voice/assist` | **Áudio → IA → Áudio** (fluxo completo) |
| `POST` | `/api/assistant/voice/assist-json` | Áudio → IA → JSON (debug) |

---

## 🧪 Exemplos de uso

### Chat por texto
```bash
curl -X POST http://localhost:8080/api/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Gastei 45 reais com almoço hoje"}'
```

**Resposta esperada:**
```json
{
  "userMessage": "Gastei 45 reais com almoço hoje",
  "assistantReply": "Anotado! Despesa de R$ 45,00 em Alimentação registrada. ✅"
}
```

### Consulta de saldo por texto
```bash
curl -X POST http://localhost:8080/api/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Qual meu saldo?"}'
```

### Fluxo completo por voz (retorna MP3)
```bash
curl -X POST http://localhost:8080/api/assistant/voice/assist \
  -F "audio=@meu-audio.mp3" \
  --output resposta.mp3
```

---

## 🏗️ Arquitetura

```
┌──────────┐    áudio    ┌──────────────┐  texto  ┌──────────────┐
│  Cliente │────────────▶│ Whisper API  │────────▶│  ChatClient  │
└──────────┘             │ (transcribe) │         │   + Tools    │
     ▲                   └──────────────┘         └──────┬───────┘
     │ áudio                                             │
     │                                                   ▼
┌──────────┐    texto    ┌──────────────┐         ┌──────────────┐
│ TTS API  │◀────────────│ AssistantSvc │◀────────│  PostgreSQL  │
│ (speak)  │             └──────────────┘         │  (Docker)    │
└──────────┘                                      └──────────────┘
```

**Tools registradas** (o LLM escolhe automaticamente):
- `addTransaction` — registra receita ou despesa
- `getBudgetSummary` — devolve saldo total
- `getCategoryTotal` — soma por categoria

---

## 🛣️ Roadmap e Auditoria (Módulo 11)

### ✅ Já implementado
- Tool Calling com 3 funções de negócio
- Memória de conversa em RAM (`InMemoryChatMemory`)
- Persistência em PostgreSQL
- Swagger/OpenAPI documentado
- Tratamento global de exceções
- Health checks via Actuator

### 🔜 Evolução recomendada
1. **Autenticação e multi-usuário** → Spring Security + JWT, isolar transações por `userId`.
2. **Memória persistente** → trocar `InMemoryChatMemory` por implementação no Postgres ou Redis.
3. **RAG** → embeddings (PGVector) com histórico de gastos para sugestões personalizadas.
4. **Auditoria completa** → tabela `audit_log` registrando cada tool call (entrada, saída, latência, custo).
5. **Observabilidade** → Micrometer + Prometheus + Grafana; rastrear tokens consumidos por requisição.
6. **Rate limiting** → Bucket4j para evitar abuso da API paga.
7. **Frontend** → app React Native que grava o áudio e consome `/voice/assist`.
8. **Testes** → Testcontainers para Postgres + WireMock para mockar a OpenAI.
9. **CI/CD** → GitHub Actions com build, testes e push da imagem Docker.
10. **Streaming de resposta** → usar `chatClient.stream()` para reduzir latência percebida.

### 🔍 Auditoria (boas práticas aplicadas)
- ✅ Validação de entrada com Bean Validation
- ✅ Separação clara em camadas (controller → service → repository)
- ✅ DTOs imutáveis (records)
- ✅ Logs estruturados com SLF4J
- ✅ Configuração externalizada (variáveis de ambiente para a chave da OpenAI)
- ✅ Dockerização do banco (infraestrutura como código)

---

## 📚 Stack
- **Java 21** • **Spring Boot 3.3.5** • **Spring AI 1.0.0-M3**
- **OpenAI**: GPT-4o-mini (chat), Whisper-1 (STT), TTS-1 voz Nova (TTS)
- **PostgreSQL 16** • **JPA/Hibernate** • **Lombok**
- **Springdoc OpenAPI 2.6** • **Maven**

---

🎓 **Bootcamp NTT DATA — Backend Java com Spring AI**
# spring-ai-inteligente
