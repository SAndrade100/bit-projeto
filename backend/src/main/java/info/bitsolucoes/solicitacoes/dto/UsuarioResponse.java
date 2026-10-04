package info.bitsolucoes.solicitacoes.dto;

import info.bitsolucoes.solicitacoes.domain.Usuario;
import info.bitsolucoes.solicitacoes.security.UsuarioAutenticado;

public record UsuarioResponse(Long id, String username, String nome) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getNome());
    }

    public static UsuarioResponse de(UsuarioAutenticado usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getNome());
    }
}
