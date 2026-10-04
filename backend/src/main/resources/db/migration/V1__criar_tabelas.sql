-- Esquema inicial do Portal de Solicitações Internas.

CREATE TABLE usuarios (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    nome        VARCHAR(120) NOT NULL,
    senha_hash  VARCHAR(100) NOT NULL,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_username UNIQUE (username)
);

CREATE TABLE categorias (
    id    SMALLSERIAL PRIMARY KEY,
    nome  VARCHAR(50) NOT NULL,
    CONSTRAINT uk_categorias_nome UNIQUE (nome)
);

CREATE TABLE solicitacoes (
    id              BIGSERIAL    PRIMARY KEY,
    titulo          VARCHAR(150) NOT NULL,
    descricao       TEXT         NOT NULL,
    categoria_id    SMALLINT     NOT NULL,
    solicitante_id  BIGINT       NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ABERTO',
    criado_em       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_solicitacoes_categoria   FOREIGN KEY (categoria_id)   REFERENCES categorias (id),
    CONSTRAINT fk_solicitacoes_solicitante FOREIGN KEY (solicitante_id) REFERENCES usuarios (id),
    CONSTRAINT ck_solicitacoes_status CHECK (status IN ('ABERTO', 'EM_ATENDIMENTO', 'CONCLUIDO')),
    CONSTRAINT ck_solicitacoes_titulo_nao_vazio CHECK (length(btrim(titulo)) > 0),
    CONSTRAINT ck_solicitacoes_descricao_nao_vazia CHECK (length(btrim(descricao)) > 0)
);

CREATE INDEX ix_solicitacoes_status        ON solicitacoes (status);
CREATE INDEX ix_solicitacoes_categoria     ON solicitacoes (categoria_id);
CREATE INDEX ix_solicitacoes_solicitante   ON solicitacoes (solicitante_id);
CREATE INDEX ix_solicitacoes_criado_em     ON solicitacoes (criado_em);
