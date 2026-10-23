# 11 — Guia de contribuição

Fluxo de trabalho do projeto.

## Fluxo

1. Escolha uma issue do [roadmap](10-roadmap.md).
2. Leia o [índice](README.md) e só os docs necessários.
3. Crie uma branch a partir de `master`.
4. Implemente com testes.
5. Atualize a documentação afetada.
6. Abra um PR e aguarde a revisão automática.

## Branches

Formato `<tipo>/<descricao-curta>`:

| Tipo | Uso |
|---|---|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `refactor` | Mudança interna sem alterar comportamento |
| `test` | Testes |
| `docs` | Documentação |
| `chore` | Build, configuração, dependências |

Exemplo: `test/transaction-service`.

## Commits

Formato `<tipo>: <resumo no imperativo>`, em uma linha de até ~70 caracteres. Referencie a issue no corpo (`Refs #N`).

## Pull requests

- Um PR por issue, pequeno e focado.
- Use `Closes #N` na descrição.
- Preencha o checklist do template (`.github/pull_request_template.md`).
- Responda ou resolva todos os comentários da revisão automática antes de fazer merge.

## Checklist antes do merge

- [ ] `./mvnw test` passa.
- [ ] Regras novas ou alteradas têm testes ([08](08-testes.md)).
- [ ] Docs afetadas foram atualizadas e o índice está correto.
- [ ] Item resolvido foi removido de [09](09-problemas-conhecidos.md).
- [ ] Nenhum segredo no código.

## Padrões de código

- Camadas e responsabilidades: [02](02-arquitetura.md).
- Dinheiro sempre `BigDecimal`.
- Erros de negócio com exceção própria + handler ([06](06-erros-e-excecoes.md)).
- Controllers sem regra de negócio.
- Endpoints novos retornam DTO, nunca entidade.
