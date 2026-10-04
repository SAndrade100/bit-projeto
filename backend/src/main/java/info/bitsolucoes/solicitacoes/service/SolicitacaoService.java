package info.bitsolucoes.solicitacoes.service;

import info.bitsolucoes.solicitacoes.domain.Categoria;
import info.bitsolucoes.solicitacoes.domain.Solicitacao;
import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import info.bitsolucoes.solicitacoes.dto.PaginaResponse;
import info.bitsolucoes.solicitacoes.dto.SolicitacaoRequest;
import info.bitsolucoes.solicitacoes.dto.SolicitacaoResponse;
import info.bitsolucoes.solicitacoes.exception.AcessoNegadoException;
import info.bitsolucoes.solicitacoes.exception.RecursoNaoEncontradoException;
import info.bitsolucoes.solicitacoes.exception.RegraDeNegocioException;
import info.bitsolucoes.solicitacoes.exception.RequisicaoInvalidaException;
import info.bitsolucoes.solicitacoes.repository.CategoriaRepository;
import info.bitsolucoes.solicitacoes.repository.FiltroSolicitacao;
import info.bitsolucoes.solicitacoes.repository.SolicitacaoRepository;
import info.bitsolucoes.solicitacoes.repository.SolicitacaoSpecs;
import info.bitsolucoes.solicitacoes.repository.UsuarioRepository;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negócio das solicitações. Todas as validações de permissão e de estado vivem aqui,
 * independentemente do que o frontend exibe.
 */
@Service
@Transactional
public class SolicitacaoService {

    private static final Sort ORDEM_PADRAO = Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("id"));

    private final SolicitacaoRepository solicitacoes;
    private final CategoriaRepository categorias;
    private final UsuarioRepository usuarios;
    private final ZoneId fuso;

    public SolicitacaoService(SolicitacaoRepository solicitacoes, CategoriaRepository categorias,
            UsuarioRepository usuarios, @Value("${app.fuso-horario}") ZoneId fuso) {
        this.solicitacoes = solicitacoes;
        this.categorias = categorias;
        this.usuarios = usuarios;
        this.fuso = fuso;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<SolicitacaoResponse> listar(FiltroSolicitacao filtro, int pagina, int tamanho, Long usuarioId) {
        if (filtro.dataInicio() != null && filtro.dataFim() != null && filtro.dataInicio().isAfter(filtro.dataFim())) {
            throw new RequisicaoInvalidaException("A data inicial não pode ser posterior à data final.");
        }
        var page = solicitacoes.findAll(SolicitacaoSpecs.comFiltro(filtro, fuso),
                PageRequest.of(pagina, tamanho, ORDEM_PADRAO));
        return PaginaResponse.de(page, s -> SolicitacaoResponse.de(s, usuarioId));
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponse buscar(Long id, Long usuarioId) {
        return SolicitacaoResponse.de(obter(id), usuarioId);
    }

    public SolicitacaoResponse criar(SolicitacaoRequest dados, Long usuarioId) {
        Solicitacao solicitacao = new Solicitacao(
                dados.titulo().trim(),
                dados.descricao().trim(),
                obterCategoria(dados.categoriaId()),
                usuarios.getReferenceById(usuarioId));
        solicitacao = solicitacoes.saveAndFlush(solicitacao);
        return SolicitacaoResponse.de(solicitacao, usuarioId);
    }

    public SolicitacaoResponse atualizar(Long id, SolicitacaoRequest dados, Long usuarioId) {
        Solicitacao solicitacao = obterEditavel(id, usuarioId, "editadas");
        solicitacao.setTitulo(dados.titulo().trim());
        solicitacao.setDescricao(dados.descricao().trim());
        solicitacao.setCategoria(obterCategoria(dados.categoriaId()));
        solicitacoes.flush();
        return SolicitacaoResponse.de(solicitacao, usuarioId);
    }

    public void excluir(Long id, Long usuarioId) {
        solicitacoes.delete(obterEditavel(id, usuarioId, "excluídas"));
        solicitacoes.flush();
    }

    /** Qualquer usuário autenticado pode avançar o status, mas apenas para o próximo passo do fluxo. */
    public SolicitacaoResponse alterarStatus(Long id, StatusSolicitacao novoStatus, Long usuarioId) {
        Solicitacao solicitacao = obter(id);
        if (!solicitacao.getStatus().podeIrPara(novoStatus)) {
            throw new RegraDeNegocioException("Não é possível alterar o status de '%s' para '%s'."
                    .formatted(solicitacao.getStatus().getDescricao(), novoStatus.getDescricao()));
        }
        solicitacao.setStatus(novoStatus);
        solicitacoes.flush();
        return SolicitacaoResponse.de(solicitacao, usuarioId);
    }

    private Solicitacao obter(Long id) {
        return solicitacoes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação %d não encontrada.".formatted(id)));
    }

    private Categoria obterCategoria(Short id) {
        return categorias.findById(id)
                .orElseThrow(() -> new RequisicaoInvalidaException("Categoria %d não existe.".formatted(id)));
    }

    /** Edição e exclusão: somente o solicitante e somente enquanto a solicitação está Aberta. */
    private Solicitacao obterEditavel(Long id, Long usuarioId, String acao) {
        Solicitacao solicitacao = obter(id);
        if (!solicitacao.getSolicitante().getId().equals(usuarioId)) {
            throw new AcessoNegadoException("Somente o solicitante pode alterar esta solicitação.");
        }
        if (!solicitacao.estaAberta()) {
            throw new RegraDeNegocioException("Apenas solicitações abertas podem ser %s.".formatted(acao));
        }
        return solicitacao;
    }
}
