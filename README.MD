# StackSpot API Integration Java Client

A robust Java client implementation for interacting with the StackSpot AI API, providing seamless integration for AI-powered conversations and code generation capabilities.

## 🚀 Features

- Token-based authentication with automatic renewal
- AI chat interactions with streaming response support
- Execution management for AI operations
- Conversation tracking and management
- Environment configuration management

## 📋 Prerequisites

- Java 17 or higher
- Maven
- StackSpot API credentials (Realm, Client ID, and Client Secret)

## ⚙️ Configuration

1. Create a `.env` file in the project root based on the provided `.env.example`:


# StackSpot API Integration — Cliente Java

Um cliente Java enxuto para integração com a API StackSpot AI. Este projeto oferece uma camada de abstração para autenticação baseada em token, conversas com o agente de IA (chat) e gerenciamento de execuções.

## Visão geral

- Linguagem: Java 17+
- Build: Maven
- Framework web: Spring Boot (controllers REST)

O objetivo é fornecer um exemplo reutilizável de como autenticar e consumir os endpoints da StackSpot AI em aplicações Java, com foco em:
- Gerenciamento seguro de tokens (renovação automática)
- Interações com agente de chat (request/response)
- Criação e acompanhamento de execuções

## Conteúdo do repositório

Principais pacotes:

- `org.stackspotapi.controller` — endpoints REST expostos pela aplicação (ex.: `ChatController`).
- `org.stackspotapi.service` — serviços que encapsulam lógica de autenticação, chat e execução.
- `org.stackspotapi.dto` — objetos de transferência de dados (requests/responses).

Estrutura (resumida):
```

src/main/java/org/stackspotapi/
├── StackSpotApiApplication.java # Ponto de entrada da aplicação
├── controller/
│ └── ChatController.java # Endpoint: /api/v1/chat
├── service/
│ ├── AiChatService.java
│ ├── EnsureTokenService.java
│ └── ExecutionService.java
└── dto/ # DTOs usados nas requests e responses

```

## Pré-requisitos

- JDK 17 ou superior
- Maven 3.6+
- Credenciais da StackSpot (Realm, Client ID, Client Secret e Agent ID)

## Variáveis de ambiente / Configuração

Crie um arquivo `.env` na raiz do projeto (ou configure variáveis de ambiente no seu ambiente de execução) com os seguintes valores:

```

STACKSPOT_REALM=seu_realm
STACKSPOT_CLIENT_ID=seu_client_id
STACKSPOT_CLIENT_SECRET=seu_client_secret
AGENT_ID=seu_agent_id

````

Observação: o projeto já faz referência ao uso de uma biblioteca para carregar variáveis de ambiente. Garanta que as credenciais não sejam comitadas no repositório.

## Como compilar e executar

# StackSpot API Integration — Cliente Java

Um cliente Java enxuto para integração com a API StackSpot AI. Este projeto oferece uma camada de abstração para autenticação baseada em token, conversas com o agente de IA (chat) e gerenciamento de execuções.

## Visão geral

- Linguagem: Java 17+
- Build: Maven
- Framework web: Spring Boot (controllers REST)

O objetivo é fornecer um exemplo reutilizável de como autenticar e consumir os endpoints da StackSpot AI em aplicações Java, com foco em:
- Gerenciamento seguro de tokens (renovação automática)
- Interações com agente de chat (request/response)
- Criação e acompanhamento de execuções

## Conteúdo do repositório

Principais pacotes:

- `org.stackspotapi.controller` — endpoints REST expostos pela aplicação (ex.: `ChatController`).
- `org.stackspotapi.service` — serviços que encapsulam lógica de autenticação, chat e execução.
- `org.stackspotapi.dto` — objetos de transferência de dados (requests/responses).

Estrutura (resumida):
```
src/main/java/org/stackspotapi/
├── StackSpotApiApplication.java    # Ponto de entrada da aplicação
├── controller/
│   └── ChatController.java        # Endpoint: /api/v1/chat
├── service/
│   ├── AiChatService.java
│   ├── EnsureTokenService.java
│   └── ExecutionService.java
└── dto/                           # DTOs usados nas requests e responses
```

## Pré-requisitos

- JDK 17 ou superior
- Maven 3.6+
- Credenciais da StackSpot (Realm, Client ID, Client Secret e Agent ID)

## Variáveis de ambiente / Configuração

Crie um arquivo `.env` na raiz do projeto (ou configure variáveis de ambiente no seu ambiente de execução) com os seguintes valores:

```
STACKSPOT_REALM=seu_realm
STACKSPOT_CLIENT_ID=seu_client_id
STACKSPOT_CLIENT_SECRET=seu_client_secret
AGENT_ID=seu_agent_id
```

Observação: o projeto já faz referência ao uso de uma biblioteca para carregar variáveis de ambiente. Garanta que as credenciais não sejam comitadas no repositório.

## Como compilar e executar

1. Compilar:

```bash
mvn clean package
```

2. Executar (modo local):

```bash
java -jar target/*.jar
```

A aplicação inicia um servidor web (Spring Boot). Por padrão, o arquivo `ChatController` expõe um endpoint HTTP:

- POST /api/v1/chat

## Endpoints (resumo)

### POST /api/v1/chat
Envia um prompt para o serviço de chat e retorna a resposta gerada pela IA.

Request (JSON):

```json
{
  "prompt": "Pergunte algo ao agente",
  "conversationId": "opcional-id-da-conversa"
}
```

Response:
- 200 OK — corpo com a resposta gerada pelo agente (texto)
- 400 Bad Request — prompt ausente ou inválido
- 500 Internal Server Error — falha de comunicação ou erro interno

Exemplo CURL:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Qual é a capital da França?"}'
```

## Observações de implementação

- `ChatController` valida se o prompt está presente e delega a chamada ao `AiChatService`.
- `AiChatService` é responsável por obter/renovar tokens (via `EnsureTokenService`) e enviar requisições para a API StackSpot.
- `ExecutionService` gerencia execuções e conversas (criação/consulta de execuções e identificação de conversationId).

## Testes

O repositório não possui testes automatizados incluídos por padrão. Recomenda-se adicionar ao menos:

- Testes unitários para serviços (Mockito + JUnit 5)
- Testes de integração para os controllers (Spring Boot Test)

## Como contribuir

1. Faça um fork do projeto
2. Crie uma branch com uma descrição curta (`feature/`, `fix/`)
3. Faça alterações e escreva testes quando aplicável
4. Abra um Pull Request descrevendo a mudança

## Segurança e boas práticas

- Nunca comite credenciais. Use `.gitignore` para `.env`.
- Habilite logs para depuração, mas evite imprimir segredos.
- Considere adicionar limites de taxa e tratamento de timeouts ao cliente HTTP.

## Próximos passos sugeridos (opcionais)

- Adicionar exemplos de uso do `AiChatService` em um pequeno cliente CLI ou testes de integração.
- Incluir GitHub Actions para build/test/scan de segurança.
- Documentar contrato JSON com OpenAPI/Swagger.

## Contato

Abra uma issue no repositório para dúvidas, bugs ou sugestões.
