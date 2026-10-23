# 07 — Configuração e execução

Arquivos: `src/main/resources/application.properties`, `pom.xml`.

## Pré-requisitos

- JDK compatível com o `pom.xml` (hoje Java 24; migração para 21 planejada na issue #6).
- PostgreSQL local com o banco `fulls_bank` criado.

## Configuração atual

| Propriedade | Valor / comportamento |
|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/fulls_bank` |
| `spring.datasource.username` | `postgres` |
| `spring.datasource.password` | definida direto no arquivo ⚠️ (issue #8: mover para variável de ambiente) |
| `spring.jpa.hibernate.ddl-auto` | `update` (esquema gerado pelo Hibernate; issue #10: Flyway) |
| `spring.jpa.show-sql` | `true` |
| `spring.jpa.properties.hibernate.format_sql` | `true` |

O H2 está no `pom.xml`, mas a aplicação usa PostgreSQL.

## Como rodar

```bash
git clone https://github.com/VictorCharro/fulls-bank-java-spring.git
cd fulls-bank-java-spring
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Como testar

```bash
./mvnw test
```

Atenção: `FullsBankApplicationTests` carrega o contexto completo e hoje exige o PostgreSQL local rodando (issue #11 resolve com Testcontainers). Para rodar só testes unitários:

```bash
./mvnw test -Dtest=TransactionServiceTest
```

## Configuração prevista

| Item | Issue |
|---|---|
| Variáveis de ambiente para o banco | #8 |
| Perfis `dev` e `test` | #9 |
| Flyway | #10 |
| Docker e docker-compose | #19 |
| CI | #20 |
| Deploy AWS | #21 |

Atualize esta página ao concluir cada item.
