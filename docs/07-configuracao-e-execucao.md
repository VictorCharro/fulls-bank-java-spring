# 07 — Configuração e execução

Arquivos: `src/main/resources/application.properties`, `application-dev.properties`, `application-test.properties`, `pom.xml`.

## Pré-requisitos

- JDK compatível com o `pom.xml` (Java 21 LTS).
- PostgreSQL local com o banco `fulls_bank` criado e, para os testes que sobem o contexto, o banco `fulls_bank_test`.

## Configuração atual

| Propriedade | Valor / comportamento |
|---|---|
| `spring.datasource.url` | `${DB_URL}`; padrão `jdbc:postgresql://localhost:5432/fulls_bank` |
| `spring.datasource.username` | `${DB_USER}`; padrão `postgres` |
| `spring.datasource.password` | `${DB_PASSWORD}`, **obrigatória** (sem padrão; se faltar, o Spring usa o texto `${DB_PASSWORD}` como senha e a conexão falha na autenticação) |
| `spring.profiles.active` | `${SPRING_PROFILES_ACTIVE}`; padrão `dev` |

As propriedades de JPA ficam nos perfis (abaixo).

## Perfis

O perfil ativo vem da variável de ambiente `SPRING_PROFILES_ACTIVE` e, se ela não existir, é `dev`.

| Perfil | Arquivo | `ddl-auto` | `show-sql` / `format_sql` | Uso |
|---|---|---|---|---|
| `dev` (padrão) | `application-dev.properties` | `update` (issue #10: Flyway) | `true` / `true` | rodar localmente |
| `test` | `application-test.properties` | `create-drop` | `false` | testes que sobem o contexto (`FullsBankApplicationTests` usa `@ActiveProfiles("test")`); usa um **banco separado**, `fulls_bank_test` (variável `TEST_DB_URL`), porque o esquema é apagado e recriado |

Exemplo: `SPRING_PROFILES_ACTIVE=test ./mvnw spring-boot:run`. Um perfil de produção virá com o deploy (issue #21).

O H2 está no `pom.xml`, mas a aplicação usa PostgreSQL.

## Variáveis de ambiente

| Variável | Uso | Padrão |
|---|---|---|
| `DB_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/fulls_bank` |
| `DB_USER` | usuário do banco | `postgres` |
| `DB_PASSWORD` | senha do banco | nenhum (obrigatória) |
| `SPRING_PROFILES_ACTIVE` | perfil ativo (`dev` ou `test`) | `dev` |
| `TEST_DB_URL` | URL JDBC do banco do perfil `test` | `jdbc:postgresql://localhost:5432/fulls_bank_test` |

Nunca coloque a senha em arquivo versionado. A senha usada antes desta mudança continua no histórico do Git: troque-a no seu PostgreSQL local.

## Como rodar

```bash
git clone https://github.com/VictorCharro/fulls-bank-java-spring.git
cd fulls-bank-java-spring
export DB_PASSWORD='sua-senha'
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Como testar

```bash
./mvnw test
```

Atenção: `FullsBankApplicationTests` carrega o contexto completo com o perfil `test` e hoje exige o PostgreSQL local rodando, o banco `fulls_bank_test` criado e `DB_PASSWORD` definida (issue #11 resolve com Testcontainers). Para rodar só testes unitários:

```bash
./mvnw test -Dtest=TransactionServiceTest
```

## Configuração prevista

| Item | Issue |
|---|---|
| Flyway | #10 |
| Docker e docker-compose | #19 |
| CI | #20 |
| Deploy AWS | #21 |

Atualize esta página ao concluir cada item.
