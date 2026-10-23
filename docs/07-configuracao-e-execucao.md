# 07 — Configuração e execução

Arquivos: `src/main/resources/application.properties`, `application-dev.properties`, `application-test.properties`, `pom.xml`.

## Pré-requisitos

- JDK compatível com o `pom.xml` (Java 21 LTS).
- PostgreSQL local com o banco `fulls_bank` criado, para rodar a aplicação.
- Docker em execução, para os testes que sobem o contexto (Testcontainers).

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

| Perfil | Arquivo | `show-sql` / `format_sql` | Uso |
|---|---|---|---|
| `dev` (padrão) | `application-dev.properties` | `true` / `true` | rodar localmente |
| `test` | `application-test.properties` | `false` | testes que sobem o contexto (`FullsBankApplicationTests` usa `@ActiveProfiles("test")`); o PostgreSQL descartável é iniciado pelo próprio teste (Testcontainers), só durante `./mvnw test`; o perfil sozinho não cria banco |

## Banco de dados (Flyway)

O esquema é criado e versionado por migrations do Flyway em `src/main/resources/db/migration` (`V1__create_initial_schema.sql` cria `client`, `account` e `transaction`). Elas rodam sozinhas ao subir a aplicação, em qualquer perfil. O Hibernate fica com `ddl-auto=validate`: se uma entidade divergir do esquema, a aplicação não sobe.

**Banco de dev que já tinha tabelas** (criadas antes do Flyway, quando o Hibernate usava `ddl-auto=update`): o Flyway se recusa a subir em um esquema não vazio sem histórico. Há duas saídas:

1. Se os dados não importam, recrie o banco (`DROP DATABASE fulls_bank; CREATE DATABASE fulls_bank;`) e suba a aplicação.
2. Para manter os dados, confira que as tabelas batem com `V1__create_initial_schema.sql` e faça o baseline uma única vez, marcando a V1 como já aplicada: `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true SPRING_FLYWAY_BASELINE_VERSION=1 ./mvnw spring-boot:run`. Depois disso, rode normalmente, sem essas variáveis.

O `baselineOnMigrate` não fica ligado na configuração do projeto: em um banco vazio a V1 precisa rodar de fato, e com a opção ligada o Flyway poderia aceitar o banco errado sem avisar.

Para mudar o esquema, crie uma nova migration (`V2__descricao.sql`, e assim por diante); nunca edite uma migration já aplicada.

O perfil `test` é para a suíte de testes; para subir a API, use `dev`. Um perfil de produção virá com o deploy (issue #21).

O H2 está no `pom.xml`, mas a aplicação usa PostgreSQL.

## Variáveis de ambiente

| Variável | Uso | Padrão |
|---|---|---|
| `DB_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/fulls_bank` |
| `DB_USER` | usuário do banco | `postgres` |
| `DB_PASSWORD` | senha do banco | nenhum (obrigatória) |
| `SPRING_PROFILES_ACTIVE` | perfil ativo (`dev` ou `test`) | `dev` |

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

`FullsBankApplicationTests` carrega o contexto completo com o perfil `test` e sobe um PostgreSQL 16 em container (Testcontainers), então precisa de Docker em execução, mas não de PostgreSQL instalado nem de `DB_PASSWORD`. Para rodar só testes unitários, sem Docker:

```bash
./mvnw test -Dtest=TransactionServiceTest
```

## Configuração prevista

| Item | Issue |
|---|---|
| Docker e docker-compose | #19 |
| CI | #20 |
| Deploy AWS | #21 |

Atualize esta página ao concluir cada item.
