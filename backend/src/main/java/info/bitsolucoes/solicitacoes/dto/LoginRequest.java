package info.bitsolucoes.solicitacoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe o usuário")
        @Size(max = 50, message = "Usuário ou senha inválidos")
        @Pattern(regexp = "^[^\\u0000]*$", message = "Usuário ou senha inválidos")
        String username,
        @NotBlank(message = "Informe a senha") String password) {
}
