# Documentação — Fulls Bank

Índice da documentação técnica. **Comece sempre por aqui**: ache o tema, leia apenas os docs indicados e abra só os arquivos de código listados.

Caminhos de código usados nesta doc:

- `MAIN` = `src/main/java/com/fulls_bank/Fulls_Bank`
- `TEST` = `src/test/java/com/fulls_bank/Fulls_Bank`

## Índice

| # | Doc | Conteúdo | Código relacionado |
|---|---|---|---|
| 01 | [Visão geral](01-visao-geral.md) | Objetivo, stack, estado atual do projeto | `pom.xml` |
| 02 | [Arquitetura](02-arquitetura.md) | Camadas, pacotes, fluxo de uma requisição, decisões | `MAIN/*` |
| 03 | [Domínio](03-dominio.md) | Entidades, relacionamentos, enums, DTOs | `MAIN/entities`, `MAIN/enums`, `MAIN/dto` |
| 04 | [API](04-api-endpoints.md) | Endpoints reais, entradas e saídas | `MAIN/controllers` |
| 05 | [Regras de negócio](05-regras-de-negocio.md) | Validações e regras por cliente, conta e transação | `MAIN/services` |
| 06 | [Erros e exceções](06-erros-e-excecoes.md) | Exceções, status HTTP, formato de erro | `MAIN/exceptions` |
| 07 | [Configuração e execução](07-configuracao-e-execucao.md) | Banco, propriedades, como rodar | `application.properties`, `pom.xml` |
| 08 | [Testes](08-testes.md) | Estratégia, convenções, o que existe e o que falta | `TEST/*` |
| 09 | [Problemas conhecidos](09-problemas-conhecidos.md) | Bugs e dívidas técnicas, ligados às issues | vários |
| 10 | [Roadmap](10-roadmap.md) | Milestones e issues por fase | — |
| 11 | [Guia de contribuição](11-guia-de-contribuicao.md) | Branches, commits, PRs, checklist | `.github/` |

## Qual doc ler por tipo de tarefa

| Tarefa | Leia |
|---|---|
| Criar ou alterar endpoint | 04, 05, 06, 03 |
| Mudar regra de negócio | 05, 06, 03 |
| Corrigir status HTTP ou mensagem de erro | 06, 04 |
| Mudar entidade, tabela ou migration | 03, 07 |
| Escrever ou corrigir testes | 08, 05 |
| Configuração, Docker, CI, AWS | 07, 10 |
| Corrigir bug conhecido | 09, depois o doc do assunto |
| Escolher a próxima tarefa | 10, 09 |
| Abrir PR | 11 |

## Regras de manutenção

1. **Mudou o código, mudou a doc no mesmo PR.** O template de PR tem esse item no checklist.
2. **Um assunto por arquivo.** Se um doc passar de ~150 linhas, divida e atualize esta tabela.
3. **Resolveu um item de [09](09-problemas-conhecidos.md)?** Remova-o de lá e atualize o doc do assunto.
4. **Novo doc?** Adicione uma linha no índice e na tabela "Qual doc ler".
5. **Documente o que o código faz hoje**, não o que se pretende fazer. Planos ficam no [roadmap](10-roadmap.md).
6. Links relativos e nenhum segredo (senhas, tokens, chaves) em nenhum arquivo.
