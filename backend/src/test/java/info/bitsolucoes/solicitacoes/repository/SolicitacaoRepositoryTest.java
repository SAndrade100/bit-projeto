package info.bitsolucoes.solicitacoes.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import info.bitsolucoes.solicitacoes.TestcontainersConfiguration;
import info.bitsolucoes.solicitacoes.domain.Categoria;
import info.bitsolucoes.solicitacoes.domain.Solicitacao;
import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import info.bitsolucoes.solicitacoes.domain.Usuario;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SolicitacaoRepositoryTest {

    @Autowired SolicitacaoRepository solicitacoes;
    @Autowired CategoriaRepository categorias;
    @Autowired UsuarioRepository usuarios;
    @Autowired JdbcTemplate jdbc;

    @Test
    void migrationsCriamCategoriasEUsuariosIniciais() {
        assertThat(categorias.findAllByOrderByNomeAsc())
                .extracting(Categoria::getNome)
                .containsExactly("Compras", "Financeiro", "Infraestrutura", "RH", "TI");
        assertThat(usuarios.findByUsername("ana.silva")).isPresent();
    }

    @Test
    void novaSolicitacaoNasceAbertaComCodigoEDatas() {
        Solicitacao salva = solicitacoes.saveAndFlush(nova("Notebook novo"));

        assertThat(salva.getStatus()).isEqualTo(StatusSolicitacao.ABERTO);
        assertThat(salva.getCodigo()).matches("SOL-\\d{6}");
        assertThat(salva.getCriadoEm()).isNotNull();
        assertThat(salva.getAtualizadoEm()).isEqualTo(salva.getCriadoEm());
    }

    @Test
    void bancoRejeitaStatusInvalido() {
        Solicitacao salva = solicitacoes.saveAndFlush(nova("Teste"));

        assertThatThrownBy(() -> jdbc.update("UPDATE solicitacoes SET status = 'INVALIDO' WHERE id = ?", salva.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void contaSolicitacoesPorStatus() {
        solicitacoes.save(nova("A"));
        solicitacoes.save(nova("B"));
        Solicitacao c = solicitacoes.save(nova("C"));
        c.setStatus(StatusSolicitacao.CONCLUIDO);
        solicitacoes.flush();

        List<SolicitacaoRepository.ContagemPorStatus> contagem = solicitacoes.contarPorStatus();

        assertThat(contagem).anySatisfy(item -> {
            assertThat(item.getStatus()).isEqualTo(StatusSolicitacao.ABERTO);
            assertThat(item.getTotal()).isEqualTo(2);
        });
        assertThat(contagem).anySatisfy(item -> {
            assertThat(item.getStatus()).isEqualTo(StatusSolicitacao.CONCLUIDO);
            assertThat(item.getTotal()).isEqualTo(1);
        });
    }

    @Test
    void gravacaoSobreLeituraDesatualizadaFalhaEmVezDeSobrescreverOStatus() {
        Solicitacao lida = solicitacoes.saveAndFlush(nova("Concorrência"));

        jdbc.update("UPDATE solicitacoes SET status = 'EM_ATENDIMENTO', versao = versao + 1 WHERE id = ?", lida.getId());

        lida.setTitulo("Título editado sobre dados velhos");

        assertThatThrownBy(() -> solicitacoes.flush()).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    private Solicitacao nova(String titulo) {
        Categoria categoria = categorias.findAllByOrderByNomeAsc().getFirst();
        Usuario solicitante = usuarios.findByUsername("ana.silva").orElseThrow();
        return new Solicitacao(titulo, "Descrição de " + titulo, categoria, solicitante);
    }
}
