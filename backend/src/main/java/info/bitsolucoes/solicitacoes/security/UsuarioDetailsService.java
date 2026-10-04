package info.bitsolucoes.solicitacoes.security;

import info.bitsolucoes.solicitacoes.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return usuarios.findByUsername(username)
                .map(u -> new UsuarioAutenticado(u.getId(), u.getUsername(), u.getNome(), u.getSenhaHash()))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
