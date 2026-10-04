-- Categorias sugeridas no enunciado.
INSERT INTO categorias (nome) VALUES
    ('TI'),
    ('RH'),
    ('Compras'),
    ('Financeiro'),
    ('Infraestrutura');

-- Usuários de demonstração (senha de ambos: senha123, armazenada com BCrypt).
-- Em produção, usuários seriam criados por um fluxo administrativo, não por migration.
INSERT INTO usuarios (username, nome, senha_hash) VALUES
    ('ana.silva',   'Ana Silva',   '$2y$10$iFQVDpDzLcx5B2.B7fKgb.P/XQDswwKHvqnboYyJmFvVO.wcFPDxG'),
    ('bruno.costa', 'Bruno Costa', '$2y$10$iFQVDpDzLcx5B2.B7fKgb.P/XQDswwKHvqnboYyJmFvVO.wcFPDxG');
