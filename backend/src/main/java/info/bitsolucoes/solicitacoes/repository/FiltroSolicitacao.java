package info.bitsolucoes.solicitacoes.repository;

import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import java.time.LocalDate;

/** Critérios opcionais de pesquisa; campos nulos são ignorados. */
public record FiltroSolicitacao(
        LocalDate dataInicio,
        LocalDate dataFim,
        Short categoriaId,
        StatusSolicitacao status,
        String titulo) {
}
