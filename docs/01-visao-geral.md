# 01 — Visão geral

API REST de um sistema bancário simplificado, feita como **projeto de portfólio** com foco em boas práticas de back-end Java: código organizado em camadas, testes automatizados, segurança, containerização e deploy.

## Funcionalidades atuais

- Cadastro e gerenciamento de clientes (CPF único).
- Abertura de contas (corrente, poupança, salário) com número gerado automaticamente.
- Transações de depósito, saque e transferência com atualização de saldo e registro histórico.
- Tratamento centralizado de erros com resposta padronizada.

Detalhes: [API](04-api-endpoints.md) e [regras de negócio](05-regras-de-negocio.md).

## Stack

| Item | Uso |
|---|---|
| Java | 24 (planejado migrar para 21 LTS, issue #6) |
| Spring Boot | 3.5.3 (Web, Data JPA, Validation) |
| Banco | PostgreSQL (driver 42.7.2); H2 presente no classpath, mas sem uso |
| Build | Maven (wrapper `mvnw`) |
| Utilitários | Lombok |
| Testes | JUnit 5, Mockito (via `spring-boot-starter-test`) |

## Estado do projeto

- Sem autenticação: todos os endpoints são públicos.
- Sem migrations: o esquema é gerado pelo Hibernate (`ddl-auto=update`).
- Sem Docker, CI ou deploy.
- Cobertura de testes mínima (ver [testes](08-testes.md)).
- Há bugs conhecidos: ver [problemas conhecidos](09-problemas-conhecidos.md).

O plano de evolução está em [roadmap](10-roadmap.md).
