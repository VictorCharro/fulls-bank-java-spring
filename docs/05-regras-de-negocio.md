# 05 — Regras de negócio

Código: `MAIN/services`. Erros lançados: [06](06-erros-e-excecoes.md).

## Autenticação (`AuthService`)

- Cadastro público (`POST /auth/register`) sempre cria usuário com papel `USER`. Administradores nascem da configuração `ADMIN_EMAIL`/`ADMIN_PASSWORD` (ver [07](07-configuracao-e-execucao.md)), nunca da API.
- E-mail é normalizado (sem espaços e em minúsculas) e é único. Duplicado lança `EmailAlreadyRegistered`.
- A senha é guardada com hash BCrypt; nunca em texto e nunca devolvida pela API.
- Login com e-mail desconhecido ou senha errada lança a mesma `InvalidCredentials`, para não revelar quais e-mails existem.
- O token é um JWT assinado (HS256) com `sub` (e-mail), `role`, `iss`, `iat` e `exp`; vale por `JWT_EXPIRATION_MINUTES` (padrão 60).
- Limitação conhecida: o usuário ainda não está ligado a um cliente. Qualquer usuário autenticado enxerga as contas e transações pelos endpoints liberados para `USER`.

## Cliente (`ClientService`)

- CPF deve ter 11 caracteres (apenas tamanho, sem validar dígitos).
- `name` e `lastName` não podem ser vazios.
- CPF duplicado lança `ExistingAccount`.
- Atualização altera somente `name` e `lastName`; o CPF nunca é alterado.
- Buscar, atualizar ou excluir cliente inexistente lança `ClientNotFound`.
- Excluir cliente que possui contas lança `ResourceInUse` (409); o cliente só pode ser excluído depois das contas. Se uma conta for criada entre a checagem e a exclusão, a violação de chave estrangeira também vira `ResourceInUse`.

## Conta (`AccountService`)

- Só cria conta para cliente existente (`ClientNotFound` caso contrário).
- Nova conta começa com saldo `0`.
- O número da conta é gerado aleatoriamente com **6 dígitos** (100000 a 999999), sem repetir. Após 20 tentativas sem achar um número livre, lança `AccountNumberUnavailable` (503).
- Busca por número inexistente lança `AccountNotFound`; busca ou exclusão por id inexistente lança `IdNotFound`.
- Excluir conta que participa de alguma transação (como origem ou destino) lança `ResourceInUse` (409).
- O `AccountService` não altera saldo: depósito, saque e transferência ficam só no `TransactionService`.

## Extrato (`TransactionService.getStatement`)

- Conta inexistente lança `IdNotFound` (404).
- Entram as transações em que a conta é origem ou destino, inclusive depósitos (sem origem) e saques (sem destino).
- `from` e `to` (datas) são opcionais e inclusivos: `from` vale a partir de 00:00:00 do dia e `to` até 23:59:59.999 do dia. `from` depois de `to` lança `InvalidPeriod` (400); `from` igual a `to` é um período de um dia.
- Ordem sempre por data decrescente, com o `id` como desempate para a paginação não repetir nem pular itens. O `sort` pedido pelo cliente é ignorado.
- Paginação: `size` padrão 20 e no máximo 100 (valores maiores são reduzidos); página além da última devolve lista vazia.

## Transação (`TransactionService.createTransaction`)

Executa em uma única transação de banco (`@Transactional` do Spring, `org.springframework.transaction.annotation`); qualquer exceção não verificada desfaz tudo.

Regra geral: `amount` nulo, zero ou negativo é rejeitado já na validação do DTO (`@NotNull` e `@Positive`), com 400. Chamado direto, o service lança `InvalidAmount` (400).

| Tipo | Campos usados | Regras | Efeito |
|---|---|---|---|
| `DEPOSIT` | `receiverId` | conta destino deve existir | soma ao saldo do destino |
| `WITHDRAWAL` | `senderId` | conta origem deve existir; saldo suficiente | subtrai do saldo da origem |
| `TRANSFER` | `senderId`, `receiverId` | ambas existem; saldo suficiente; contas diferentes (`SameAccountTransfer`, 400) | subtrai da origem e soma ao destino |

Em seguida é gravado um registro `Transaction` com tipo, valor, contas envolvidas e `timestamp` atual.

Observações:

- Em `TRANSFER`, a checagem de saldo vem **antes** da checagem de contas iguais.
- Contas inexistentes lançam `AccountNotFound`.
- Buscar transação por id inexistente lança `TransactionNotFound` (404).
- Se qualquer regra falhar, nenhum saldo é alterado e nenhuma transação é gravada.
- Concorrência: `Account` tem `@Version` (lock otimista). Se outra operação alterar a mesma conta entre a leitura e a gravação, a transação inteira é desfeita e a API responde `409`; o cliente deve repetir a requisição. O mesmo vale para o deadlock do banco entre transferências em sentidos opostos (A→B e B→A ao mesmo tempo).
