package info.bitsolucoes.solicitacoes.dto;

import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(@NotNull(message = "Informe o status") StatusSolicitacao status) {
}
