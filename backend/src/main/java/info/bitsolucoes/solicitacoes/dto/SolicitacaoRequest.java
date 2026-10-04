package info.bitsolucoes.solicitacoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SolicitacaoRequest(
        @NotBlank(message = "Informe o título")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        @Pattern(regexp = SEM_NUL, message = "O título contém caracteres inválidos")
        String titulo,

        @NotBlank(message = "Informe a descrição")
        @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres")
        @Pattern(regexp = SEM_NUL, message = "A descrição contém caracteres inválidos")
        String descricao,

        @NotNull(message = "Informe a categoria")
        Short categoriaId) {

    /** O PostgreSQL não aceita o caractere NUL (código 0) em textos; sem esta validação ele viraria erro 500. */
    private static final String SEM_NUL = "^[^\\u0000]*$";
}
