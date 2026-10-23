# 09 — Problemas conhecidos

Bugs e dívidas técnicas do código **atual**. Ao resolver um item, remova-o daqui e atualize o doc do assunto ([regra de manutenção](README.md#regras-de-manutenção)).

Legenda de issue: `#N` = já existe; "sem issue" = ainda precisa ser criada.

## Bugs

| # | Problema | Onde | Issue |
|---|---|---|---|

## Qualidade de código

| # | Problema | Issue |
|---|---|---|
| Q5 | `Account.id` e `Account.type` públicos; `Transaction.description` sem uso; `senderId`/`receiverId` do DTO com padrão `0L` tornam `@NotNull` inócuo. | sem issue |
