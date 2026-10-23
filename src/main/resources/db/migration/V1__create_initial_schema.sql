CREATE TABLE client (
    id        BIGSERIAL PRIMARY KEY,
    cpf       VARCHAR(11) UNIQUE,
    name      VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

CREATE TABLE account (
    id             BIGSERIAL PRIMARY KEY,
    account_number INTEGER NOT NULL UNIQUE,
    balance        NUMERIC(38, 2) NOT NULL,
    type           VARCHAR(255) CHECK (type IN ('CHECKING', 'SAVINGS', 'SALARY')),
    client_id      BIGINT NOT NULL REFERENCES client (id)
);

CREATE TABLE transaction (
    id          BIGSERIAL PRIMARY KEY,
    amount      NUMERIC(38, 2),
    timestamp   TIMESTAMP(6),
    description VARCHAR(255),
    type        VARCHAR(255) CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    sender_id   BIGINT REFERENCES account (id),
    receiver_id BIGINT REFERENCES account (id)
);
