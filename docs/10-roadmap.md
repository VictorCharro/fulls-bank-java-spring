# 10 — Roadmap

As tarefas vivem nas **issues** do GitHub, agrupadas em milestones. Esta página é o mapa; o detalhe e o checklist de cada item estão na issue.

| Milestone | Issues | Foco |
|---|---|---|
| Semana 1 — Testes unitários | #1, #2 | Testes de `TransactionService`, `AccountService`, `ClientService` |
| Semana 2 — Refatoração | #3, #4, #5, #6, #7 | DTO em transações, exceções próprias, `@Transactional` do Spring, Java 21, README |
| Semana 3 — Configuração e banco | #8, #9, #10, #11, #12 | Segredos fora do código, perfis, Flyway, Testcontainers, testes de integração |
| Semana 4 — Concorrência | #13, #14 | Lock otimista e teste de concorrência |
| Fase 2 — Segurança e infra | #15 a #20 | JWT, extrato com filtro, Swagger, MapStruct, Docker, CI |
| Fase 3 — AWS | #21, #22 | Deploy com RDS, logs e alarme no CloudWatch |

## Como escolher a próxima tarefa

1. Siga a ordem dos milestones.
2. Leia a issue e os docs indicados em [Qual doc ler](README.md#qual-doc-ler-por-tipo-de-tarefa).
3. Confira se o item também aparece em [problemas conhecidos](09-problemas-conhecidos.md).
4. Ao concluir: atualize os docs afetados e remova o item de [09](09-problemas-conhecidos.md).

## Bugs ainda sem issue

Ver itens "sem issue" em [09](09-problemas-conhecidos.md). Crie a issue antes de corrigir e adicione-a ao milestone adequado.
