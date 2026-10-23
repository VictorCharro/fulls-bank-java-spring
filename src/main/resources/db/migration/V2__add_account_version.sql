-- Coluna de versão para o lock otimista (@Version) de Account
ALTER TABLE account ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
