package info.bitsolucoes.solicitacoes.domain;

import static info.bitsolucoes.solicitacoes.domain.StatusSolicitacao.ABERTO;
import static info.bitsolucoes.solicitacoes.domain.StatusSolicitacao.CONCLUIDO;
import static info.bitsolucoes.solicitacoes.domain.StatusSolicitacao.EM_ATENDIMENTO;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatusSolicitacaoTest {

    @Test
    void fluxoEhSequencial() {
        assertThat(ABERTO.proximo()).contains(EM_ATENDIMENTO);
        assertThat(EM_ATENDIMENTO.proximo()).contains(CONCLUIDO);
        assertThat(CONCLUIDO.proximo()).isEmpty();
    }

    @Test
    void soPermiteAvancarParaOProximo() {
        assertThat(ABERTO.podeIrPara(EM_ATENDIMENTO)).isTrue();
        assertThat(ABERTO.podeIrPara(CONCLUIDO)).isFalse();
        assertThat(CONCLUIDO.podeIrPara(ABERTO)).isFalse();
        assertThat(EM_ATENDIMENTO.podeIrPara(EM_ATENDIMENTO)).isFalse();
    }
}
