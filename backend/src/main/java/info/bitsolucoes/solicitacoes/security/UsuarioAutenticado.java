package info.bitsolucoes.solicitacoes.security;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/** Principal da sessão: além do login, carrega o id e o nome para uso nas regras de negócio. */
public class UsuarioAutenticado extends User {

    private final Long id;
    private final String nome;

    public UsuarioAutenticado(Long id, String username, String nome, String senhaHash) {
        super(username, senhaHash, List.of(new SimpleGrantedAuthority("ROLE_USUARIO")));
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
