package info.bitsolucoes.solicitacoes.repository;

import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import java.time.LocalDate;

public record FiltroSolicitacao(
        LocalDate dataInicio,
        LocalDate dataFim,
        Short categoriaId,
        StatusSolicitacao status,
        String titulo) {
}
