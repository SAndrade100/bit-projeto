package info.bitsolucoes.solicitacoes.domain;

import java.util.Optional;

/**
 * Ciclo de vida de uma solicitação. O fluxo é sequencial:
 * ABERTO -> EM_ATENDIMENTO -> CONCLUIDO.
 */
public enum StatusSolicitacao {
    ABERTO("Aberto"),
    EM_ATENDIMENTO("Em Atendimento"),
    CONCLUIDO("Concluído");

    private final String descricao;

    StatusSolicitacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Próximo status do fluxo, ou vazio se este já é o final. */
    public Optional<StatusSolicitacao> proximo() {
        return switch (this) {
            case ABERTO -> Optional.of(EM_ATENDIMENTO);
            case EM_ATENDIMENTO -> Optional.of(CONCLUIDO);
            case CONCLUIDO -> Optional.empty();
        };
    }

    public boolean podeIrPara(StatusSolicitacao destino) {
        return proximo().filter(destino::equals).isPresent();
    }
}
