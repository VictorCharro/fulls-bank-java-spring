# 09 — Problemas conhecidos

Bugs e dívidas técnicas do código **atual**. Ao resolver um item, remova-o daqui e atualize o doc do assunto ([regra de manutenção](README.md#regras-de-manutenção)).

Legenda de issue: `#N` = já existe; "sem issue" = ainda precisa ser criada.

## Bugs

| # | Problema | Onde | Issue |
|---|---|---|---|

## Segurança

| # | Problema | Onde | Issue |
|---|---|---|---|
| S1 | O usuário ainda não está ligado a um cliente, então qualquer usuário logado consegue consultar e movimentar qualquer conta (não há checagem de dono). Os `DELETE` e as listas completas já são só do `ADMIN`. | `AccountService`, `TransactionService`, `ClientService` | sem issue |

## Qualidade de código

| # | Problema | Issue |
|---|---|---|
