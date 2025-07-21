# 🏦 Fulls Bank - API REST com Spring Boot

Este é um projeto de estudo que simula um sistema bancário básico, desenvolvido em Java com Spring Boot. A aplicação permite o cadastro e gerenciamento de clientes, contas bancárias e transações financeiras, com foco em boas práticas de back-end corporativo.

---

## 🚀 Funcionalidades implementadas

### 👤 Cliente
- Criar cliente (`POST /client`)
- Listar todos os clientes (`GET /client`)
- Buscar cliente por ID (`GET /client/{id}`)
- Excluir cliente (`DELETE /client/{id}`)
- Validação de CPF único
- Validação com Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Size`)

---

### 💳 Conta
- Criar conta com número gerado automaticamente (`POST /account`)
- Buscar conta por ID (`GET /account/{id}`)
- Buscar conta por número da conta (`GET /account/number/{num}`)
- Saque (`POST /account/withdrawal`)
- Depósito (`POST /account/deposit`)
- Excluir conta (`DELETE /account/{id}`)
- Validação de saldo inicial
- Associa cliente existente à conta (via ID)
- Uso de DTOs (Request/Response)
- Evita laços infinitos de serialização (loop) nas respostas com transações

---

### 🔄 Transações
- Transação registrada automaticamente no saque e depósito
- Buscar transação por ID (`GET /transaction/{id}`)
- Listar todas as transações (`GET /transaction`)

---

## ❌ Tratamento de exceções

Tratamento centralizado com `@RestControllerAdvice` e `@ExceptionHandler`, retornando mensagens amigáveis, HTTP status adequado e timestamp:

- `ClientNotFound`
- `AccountNotFound`
- `ExistingAccount`
- `IdNotFound`
- `InsufficientBalance`
- `NegativeAmount`
- `MethodArgumentNotValidException`

---

## 🛠️ Tecnologias utilizadas

- Java 24
- Spring Boot
- Spring Data JPA
- Hibernate
- Lombok
- Jakarta Bean Validation
- H2 Database (banco em memória)
- Maven

---

## 📁 Estrutura de pacotes

- `entities/` → Entidades JPA: `Client`, `Account`, `Transaction`
- `controllers/` → Endpoints REST
- `services/` → Regras de negócio (camada de serviço)
- `repositories/` → Interfaces de persistência
- `dto/` → Objetos de transferência de dados (Request/Response)
- `exceptions/` → Exceções customizadas e handler global
- `enums/` → Enumerações para tipo de conta e tipo de transação

---

## 🔧 Como rodar o projeto

1. **Clone o repositório:**

```bash
git clone https://github.com/VictorCharro/fulls-bank-java-spring.git
cd fulls-bank-java-spring

```
---

## 📌 Próximos passos
✅ Criar entidade e CRUD de transações

✅ Associar transações com saque e depósito automaticamente

✅ Implementar uso de DTOs (Request/Response)

✅ Resolver loops de serialização entre Account ↔ Transaction

---

## 🚧 Em desenvolvimento:

🔁 Implementar transferência entre contas com movimentação de saldo e gravação da transação (POST /account/transfer)

📄 Documentação com Swagger/OpenAPI

🧪 Testes unitários e de integração com JUnit e Mockito

🗃️ Persistência com PostgreSQL (substituir H2 futuramente)

🔐 Autenticação de usuários com Spring Security (login e permissões)

📊 Filtros para extrato bancário por data (query param)

🧠 Camada de mapeamento com ModelMapper ou MapStruct (refatorar DTOs)
