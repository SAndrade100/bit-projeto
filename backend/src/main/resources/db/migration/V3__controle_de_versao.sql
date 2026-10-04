-- Controle de concorrência otimista (JPA @Version): impede que duas transações simultâneas
-- sobrescrevam uma à outra (ex.: edição de uma solicitação enquanto seu status é alterado).
ALTER TABLE solicitacoes ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
