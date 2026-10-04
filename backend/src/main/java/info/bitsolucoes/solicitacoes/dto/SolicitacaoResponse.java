package info.bitsolucoes.solicitacoes.dto;

import info.bitsolucoes.solicitacoes.domain.Solicitacao;
import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import java.time.OffsetDateTime;

/**
 * {@code editavel} indica se o usuário atual pode editar/excluir (dono e status Aberto) e
 * {@code proximoStatus} qual a única transição permitida; assim o frontend não replica as regras.
 */
public record SolicitacaoResponse(
        Long id,
        String codigo,
        String titulo,
        String descricao,
        CategoriaResponse categoria,
        UsuarioResponse solicitante,
        StatusSolicitacao status,
        String statusDescricao,
        StatusSolicitacao proximoStatus,
        boolean editavel,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {

    public static SolicitacaoResponse de(Solicitacao s, Long usuarioId) {
        boolean dono = s.getSolicitante().getId().equals(usuarioId);
        return new SolicitacaoResponse(
                s.getId(),
                s.getCodigo(),
                s.getTitulo(),
                s.getDescricao(),
                CategoriaResponse.de(s.getCategoria()),
                UsuarioResponse.de(s.getSolicitante()),
                s.getStatus(),
                s.getStatus().getDescricao(),
                s.getStatus().proximo().orElse(null),
                dono && s.estaAberta(),
                s.getCriadoEm(),
                s.getAtualizadoEm());
    }
}
