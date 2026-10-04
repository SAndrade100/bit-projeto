package info.bitsolucoes.solicitacoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitacaoRequest(
        @NotBlank(message = "Informe o título")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String titulo,

        @NotBlank(message = "Informe a descrição")
        @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres")
        String descricao,

        @NotNull(message = "Informe a categoria")
        Short categoriaId) {
}
