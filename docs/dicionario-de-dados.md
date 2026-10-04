# Dicionário de Dados

Banco: **PostgreSQL 16**. O esquema é criado e versionado pelo **Flyway**; os scripts SQL
(entregáveis do enunciado) ficam em [`backend/src/main/resources/db/migration`](../backend/src/main/resources/db/migration):

| Script | Conteúdo |
|---|---|
| `V1__criar_tabelas.sql` | Tabelas, chaves, constraints e índices |
| `V2__dados_iniciais.sql` | Categorias do enunciado e usuários de demonstração |

As migrations rodam automaticamente na subida da API. O Hibernate apenas **valida** o esquema (`ddl-auto=validate`).

## Modelo

```
usuarios 1 ──< solicitacoes >── 1 categorias
```

## Tabela `usuarios`

| Coluna | Tipo | Nulo | Descrição |
|---|---|---|---|
| `id` | BIGSERIAL (PK) | não | Identificador |
| `username` | VARCHAR(50) | não | Login. Único (`uk_usuarios_username`) |
| `nome` | VARCHAR(120) | não | Nome exibido |
| `senha_hash` | VARCHAR(100) | não | Senha com hash BCrypt (nunca em texto puro) |
| `criado_em` | TIMESTAMPTZ | não | Data de criação (padrão `now()`) |

## Tabela `categorias`

| Coluna | Tipo | Nulo | Descrição |
|---|---|---|---|
| `id` | SMALLSERIAL (PK) | não | Identificador |
| `nome` | VARCHAR(50) | não | Nome da categoria. Único (`uk_categorias_nome`) |

Valores iniciais: TI, RH, Compras, Financeiro, Infraestrutura.

## Tabela `solicitacoes`

| Coluna | Tipo | Nulo | Descrição |
|---|---|---|---|
| `id` | BIGSERIAL (PK) | não | Identificador. O código exibido é derivado dele: `SOL-` + id com 6 dígitos (ex.: `SOL-000123`) |
| `titulo` | VARCHAR(150) | não | Título. Não pode ser vazio (`ck_solicitacoes_titulo_nao_vazio`) |
| `descricao` | TEXT | não | Descrição detalhada. Não pode ser vazia |
| `categoria_id` | SMALLINT (FK) | não | Referencia `categorias.id` |
| `solicitante_id` | BIGINT (FK) | não | Referencia `usuarios.id`; definido pelo servidor a partir do usuário logado |
| `status` | VARCHAR(20) | não | `ABERTO` (padrão), `EM_ATENDIMENTO` ou `CONCLUIDO` (`ck_solicitacoes_status`) |
| `criado_em` | TIMESTAMPTZ | não | Data de abertura, definida pelo servidor |
| `atualizado_em` | TIMESTAMPTZ | não | Última alteração, definida pelo servidor |

### Regras e decisões de modelagem
- **Status como texto com CHECK** em vez de tabela de domínio: são apenas três valores fixos, ligados à regra de negócio (o fluxo é sequencial: Aberto → Em Atendimento → Concluído), então ficam no enum Java e protegidos por constraint no banco.
- **Categorias em tabela**, pois a lista pode evoluir sem alterar código.
- **Código da solicitação não é coluna**: é derivado do `id`, o que evita duplicidade e dessincronia.
- **TIMESTAMPTZ** em todas as datas, para evitar ambiguidade de fuso horário nos filtros por período.

### Índices
| Índice | Coluna | Motivo |
|---|---|---|
| `ix_solicitacoes_status` | `status` | Filtro e contagem do dashboard |
| `ix_solicitacoes_categoria` | `categoria_id` | Filtro por categoria |
| `ix_solicitacoes_solicitante` | `solicitante_id` | Junção e verificação de autoria |
| `ix_solicitacoes_criado_em` | `criado_em` | Filtro por período e ordenação |

## Usuários de demonstração
| Usuário | Senha |
|---|---|
| `ana.silva` | `senha123` |
| `bruno.costa` | `senha123` |

Existem apenas para avaliação local.
