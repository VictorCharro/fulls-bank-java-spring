# 09 — Problemas conhecidos

Bugs e dívidas técnicas do código **atual**. Ao resolver um item, remova-o daqui e atualize o doc do assunto ([regra de manutenção](README.md#regras-de-manutenção)).

Legenda de issue: `#N` = já existe; "sem issue" = ainda precisa ser criada.

## Bugs

| # | Problema | Onde | Issue |
|---|---|---|---|

## Segurança e configuração

| # | Problema | Issue |
|---|---|---|
| S2 | Sem autenticação nem autorização. | #15 |

## Concorrência

| # | Problema | Issue |
|---|---|---|
| C1 | Falta um teste que comprove o lock otimista com requisições simultâneas de saque e transferência. | #14 |

## Qualidade de código

| # | Problema | Issue |
|---|---|---|
| Q1 | Endpoints de cliente expõem a entidade `Client` (sem DTO). | sem issue |
| Q5 | `Account.id` e `Account.type` públicos; `Transaction.description` sem uso; `senderId`/`receiverId` do DTO com padrão `0L` tornam `@NotNull` inócuo. | sem issue |
| Q6 | Mapeamento manual de DTOs. | #18 |
