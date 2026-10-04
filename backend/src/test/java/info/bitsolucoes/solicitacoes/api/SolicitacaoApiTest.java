package info.bitsolucoes.solicitacoes.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import info.bitsolucoes.solicitacoes.TestcontainersConfiguration;
import info.bitsolucoes.solicitacoes.domain.Categoria;
import info.bitsolucoes.solicitacoes.domain.Solicitacao;
import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import info.bitsolucoes.solicitacoes.domain.Usuario;
import info.bitsolucoes.solicitacoes.repository.CategoriaRepository;
import info.bitsolucoes.solicitacoes.repository.SolicitacaoRepository;
import info.bitsolucoes.solicitacoes.repository.UsuarioRepository;
import info.bitsolucoes.solicitacoes.security.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/** Testes de ponta a ponta da API (HTTP -> serviço -> PostgreSQL), revertidos ao fim de cada teste. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class SolicitacaoApiTest {

    static final String JSON_VALIDO = """
            {"titulo":"Trocar monitor","descricao":"Monitor com defeito","categoriaId":%d}""";

    @Autowired MockMvc mvc;
    @Autowired SolicitacaoRepository solicitacoes;
    @Autowired CategoriaRepository categorias;
    @Autowired UsuarioRepository usuarios;

    Usuario ana;
    Usuario bruno;
    Categoria ti;
    Categoria rh;

    @BeforeEach
    void preparar() {
        ana = usuarios.findByUsername("ana.silva").orElseThrow();
        bruno = usuarios.findByUsername("bruno.costa").orElseThrow();
        ti = categorias.findAllByOrderByNomeAsc().stream().filter(c -> c.getNome().equals("TI")).findFirst().orElseThrow();
        rh = categorias.findAllByOrderByNomeAsc().stream().filter(c -> c.getNome().equals("RH")).findFirst().orElseThrow();
    }

    // ---------- criação

    @Test
    void criaSolicitacaoAbertaComSolicitanteDaSessao() throws Exception {
        mvc.perform(post("/api/solicitacoes").with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO.formatted(ti.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(org.hamcrest.Matchers.matchesPattern("SOL-\\d{6}")))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.solicitante.username").value("ana.silva"))
                .andExpect(jsonPath("$.categoria.nome").value("TI"))
                .andExpect(jsonPath("$.editavel").value(true))
                .andExpect(jsonPath("$.proximoStatus").value("EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.criadoEm").exists());
    }

    @Test
    void naoPermiteInformarSolicitanteOuStatusNaCriacao() throws Exception {
        String corpo = """
                {"titulo":"X","descricao":"Y","categoriaId":%d,"status":"CONCLUIDO","solicitanteId":%d}"""
                .formatted(ti.getId(), bruno.getId());

        mvc.perform(post("/api/solicitacoes").with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.solicitante.username").value("ana.silva"));
    }

    @Test
    void criacaoInvalidaRetornaErrosPorCampo() throws Exception {
        mvc.perform(post("/api/solicitacoes").with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"  \",\"descricao\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.titulo").exists())
                .andExpect(jsonPath("$.erros.descricao").exists())
                .andExpect(jsonPath("$.erros.categoriaId").exists());
    }

    @Test
    void categoriaInexistenteRetorna400() throws Exception {
        mvc.perform(post("/api/solicitacoes").with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO.formatted(999)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void jsonMalformadoRetorna400ComMensagemEmPortugues() throws Exception {
        mvc.perform(post("/api/solicitacoes").with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{titulo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O corpo da requisição está ausente ou mal formatado."));
    }

    @Test
    void caractereNulNosTextosRetorna400EmVezDeErroDeBanco() throws Exception {
        mvc.perform(post("/api/solicitacoes").with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"a\\u0000b\",\"descricao\":\"x\\u0000\",\"categoriaId\":%d}".formatted(ti.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.titulo").exists())
                .andExpect(jsonPath("$.erros.descricao").exists());
    }

    @Test
    void escritaSemCsrfRetorna403() throws Exception {
        mvc.perform(post("/api/solicitacoes").with(como(ana))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO.formatted(ti.getId())))
                .andExpect(status().isForbidden());
    }

    // ---------- edição e exclusão

    @Test
    void donoEditaSolicitacaoAberta() throws Exception {
        Solicitacao s = salvar(ana, StatusSolicitacao.ABERTO);

        mvc.perform(put("/api/solicitacoes/" + s.getId()).with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO.formatted(rh.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Trocar monitor"))
                .andExpect(jsonPath("$.categoria.nome").value("RH"));
    }

    @Test
    void naoEditaSolicitacaoEmAtendimento() throws Exception {
        Solicitacao s = salvar(ana, StatusSolicitacao.EM_ATENDIMENTO);

        mvc.perform(put("/api/solicitacoes/" + s.getId()).with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO.formatted(ti.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    void outroUsuarioNaoEditaNemExclui() throws Exception {
        Solicitacao s = salvar(ana, StatusSolicitacao.ABERTO);

        mvc.perform(put("/api/solicitacoes/" + s.getId()).with(como(bruno)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO.formatted(ti.getId())))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/solicitacoes/" + s.getId()).with(como(bruno)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void donoExcluiSolicitacaoAberta() throws Exception {
        Solicitacao s = salvar(ana, StatusSolicitacao.ABERTO);

        mvc.perform(delete("/api/solicitacoes/" + s.getId()).with(como(ana)).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/solicitacoes/" + s.getId()).with(como(ana))).andExpect(status().isNotFound());
    }

    @Test
    void naoExcluiSolicitacaoConcluida() throws Exception {
        Solicitacao s = salvar(ana, StatusSolicitacao.CONCLUIDO);

        mvc.perform(delete("/api/solicitacoes/" + s.getId()).with(como(ana)).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Apenas solicitações abertas podem ser excluídas."));
    }

    @Test
    void solicitacaoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/solicitacoes/999999").with(como(ana))).andExpect(status().isNotFound());
        mvc.perform(delete("/api/solicitacoes/999999").with(como(ana)).with(csrf())).andExpect(status().isNotFound());
    }

    // ---------- status

    @Test
    void qualquerUsuarioAvancaOStatusSequencialmente() throws Exception {
        Solicitacao s = salvar(ana, StatusSolicitacao.ABERTO);

        mvc.perform(patch("/api/solicitacoes/" + s.getId() + "/status").with(como(bruno)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"EM_ATENDIMENTO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.editavel").value(false));

        mvc.perform(patch("/api/solicitacoes/" + s.getId() + "/status").with(como(bruno)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONCLUIDO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proximoStatus").doesNotExist());
    }

    @Test
    void naoPermitePularNemVoltarStatus() throws Exception {
        Solicitacao aberta = salvar(ana, StatusSolicitacao.ABERTO);
        Solicitacao concluida = salvar(ana, StatusSolicitacao.CONCLUIDO);

        mvc.perform(patch("/api/solicitacoes/" + aberta.getId() + "/status").with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONCLUIDO\"}"))
                .andExpect(status().isConflict());
        mvc.perform(patch("/api/solicitacoes/" + concluida.getId() + "/status").with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ABERTO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void statusInvalidoRetorna400() throws Exception {
        Solicitacao s = salvar(ana, StatusSolicitacao.ABERTO);

        mvc.perform(patch("/api/solicitacoes/" + s.getId() + "/status").with(como(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"NADA\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- listagem e filtros

    @Test
    void listaComPaginacaoOrdenadaDaMaisRecente() throws Exception {
        long antes = solicitacoes.count();
        for (int i = 1; i <= 3; i++) {
            salvar(ana, StatusSolicitacao.ABERTO, "Pedido " + i, ti);
        }

        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.conteudo[0].titulo").value("Pedido 3"))
                .andExpect(jsonPath("$.totalElementos").value(antes + 3))
                .andExpect(jsonPath("$.tamanho").value(2));
    }

    @Test
    void filtraPorStatusCategoriaETitulo() throws Exception {
        salvar(ana, StatusSolicitacao.ABERTO, "Impressora quebrada", ti);
        salvar(ana, StatusSolicitacao.CONCLUIDO, "Impressora nova", ti);
        salvar(bruno, StatusSolicitacao.ABERTO, "Férias de julho", rh);

        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("status", "ABERTO").param("titulo", "IMPRESSORA"))
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].titulo").value("Impressora quebrada"));

        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("categoriaId", rh.getId().toString()))
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].titulo").value("Férias de julho"));
    }

    @Test
    void curingasDoLikeNaoSaoInterpretados() throws Exception {
        salvar(ana, StatusSolicitacao.ABERTO, "Reembolso 100% pendente", ti);
        salvar(ana, StatusSolicitacao.ABERTO, "Outro assunto", ti);

        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("titulo", "%"))
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].titulo").value("Reembolso 100% pendente"));
    }

    @Test
    void filtraPorPeriodoComFimInclusivo() throws Exception {
        salvar(ana, StatusSolicitacao.ABERTO, "De hoje", ti);
        String hoje = java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString();
        String ontem = java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).minusDays(1).toString();

        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("dataInicio", hoje).param("dataFim", hoje))
                .andExpect(jsonPath("$.conteudo[?(@.titulo == 'De hoje')]", hasSize(1)));
        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("dataInicio", ontem).param("dataFim", ontem))
                .andExpect(jsonPath("$.conteudo[?(@.titulo == 'De hoje')]", hasSize(0)));
    }

    @Test
    void periodoInvertidoOuParametroInvalidoRetornaBadRequest() throws Exception {
        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("dataInicio", "2026-10-10").param("dataFim", "2026-10-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("status", "XYZ"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("dataInicio", "ontem"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("tamanho", "1000"))
                .andExpect(status().isBadRequest());
        // offset (pagina * tamanho) estouraria o limite de inteiro do Spring Data e viraria 500
        mvc.perform(get("/api/solicitacoes").with(como(ana)).param("pagina", "2147483647").param("tamanho", "50"))
                .andExpect(status().isBadRequest());
    }

    // ---------- dashboard e categorias

    @Test
    void dashboardContaPorStatus() throws Exception {
        long abertas = solicitacoes.count();
        salvar(ana, StatusSolicitacao.ABERTO);
        salvar(ana, StatusSolicitacao.EM_ATENDIMENTO);
        salvar(bruno, StatusSolicitacao.CONCLUIDO);
        salvar(bruno, StatusSolicitacao.CONCLUIDO);

        mvc.perform(get("/api/dashboard").with(como(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(abertas + 4))
                .andExpect(jsonPath("$.abertas").value(1))
                .andExpect(jsonPath("$.emAtendimento").value(1))
                .andExpect(jsonPath("$.concluidas").value(2));
    }

    @Test
    void listaCategoriasEmOrdemAlfabetica() throws Exception {
        mvc.perform(get("/api/categorias").with(como(ana)))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].nome").value("Compras"));
    }

    // ---------- apoio

    private RequestPostProcessor como(Usuario u) {
        return user(new UsuarioAutenticado(u.getId(), u.getUsername(), u.getNome(), u.getSenhaHash()));
    }

    private Solicitacao salvar(Usuario dono, StatusSolicitacao status) {
        return salvar(dono, status, "Solicitação de teste", ti);
    }

    private Solicitacao salvar(Usuario dono, StatusSolicitacao status, String titulo, Categoria categoria) {
        Solicitacao s = new Solicitacao(titulo, "Descrição", categoria, dono);
        s.setStatus(status);
        return solicitacoes.saveAndFlush(s);
    }
}
