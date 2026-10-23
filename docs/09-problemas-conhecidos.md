# 09 — Problemas conhecidos

Bugs e dívidas técnicas do código **atual**. Ao resolver um item, remova-o daqui e atualize o doc do assunto ([regra de manutenção](README.md#regras-de-manutenção)).

Legenda de issue: `#N` = já existe; "sem issue" = ainda precisa ser criada.

## Bugs

| # | Problema | Onde | Issue |
|---|---|---|---|
| B2 | `POST /account/withdrawal`: parâmetros sem `@RequestParam`, não registra `Transaction` e não existe endpoint de depósito equivalente. Duplica a função de `POST /transaction`. | `AccountController`, `AccountService` | sem issue |
| B3 | `IdNotFound` e `IllegalArgumentException` não têm handler e retornam 500. | `MAIN/exceptions/GlobalExceptionHandler` | parcial (#4) |
| B4 | `GET /transaction/{id}` retorna a entidade, sem 404 explícito. | `TransactionController` | #3 |
| B5 | Excluir conta com transações, ou cliente com contas, viola chave estrangeira e retorna 500. | `AccountService`, `ClientService` | sem issue |
| B6 | Número da conta tem só 9000 valores possíveis (1000–9999); ao esgotar, o loop de geração não termina. | `AccountService.generateUniqueAccountNumber` | sem issue |
| B7 | `deposit` e `withdrawal` do `AccountService` aceitam valor zero. | `AccountService` | sem issue |
| B8 | `amount` nulo passa pela validação do DTO (`@Positive` aceita nulo) e cai no 500 do B3. | `TransactionRequestDTO` | sem issue |

## Segurança e configuração

| # | Problema | Issue |
|---|---|---|
| S1 | Senha do banco commitada em `application.properties`. | #8 |
| S2 | Sem autenticação nem autorização. | #15 |
| S3 | `ddl-auto=update` em vez de migrations. | #10 |

## Concorrência

| # | Problema | Issue |
|---|---|---|
| C1 | Saldo atualizado sem lock nem `@Version`; duas operações simultâneas podem causar perda de atualização. | #13, #14 |

## Qualidade de código

| # | Problema | Issue |
|---|---|---|
| Q1 | Endpoints de cliente expõem a entidade `Client` (sem DTO). | sem issue |
| Q2 | `TransactionService` usa `jakarta.transaction.Transactional` em vez do do Spring. | #5 |
| Q3 | Java 24 (não LTS). | #6 |
| Q4 | `IllegalArgumentException` genérica em regras de negócio. | #4 |
| Q5 | `Account.id` e `Account.type` públicos; `Transaction.description` sem uso; `senderId`/`receiverId` do DTO com padrão `0L` tornam `@NotNull` inócuo. | sem issue |
| Q6 | Mapeamento manual de DTOs. | #18 |
| Q7 | `FullsBankApplicationTests` depende de banco local. | #11 |
| Q8 | README raiz desatualizado. | #7 |
| Q9 | Duplicidade de lógica de saldo entre `AccountService` e `TransactionService`. | sem issue |
