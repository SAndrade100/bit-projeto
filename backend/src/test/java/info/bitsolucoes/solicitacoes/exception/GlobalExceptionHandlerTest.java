package info.bitsolucoes.solicitacoes.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ProblemDetail;

class GlobalExceptionHandlerTest {

    @Test
    void conflitoDeVersaoViraHttp409ComMensagemClara() {
        ProblemDetail problema = new GlobalExceptionHandler()
                .conflitoDeVersao(new OptimisticLockingFailureException("versão antiga"));

        assertThat(problema.getStatus()).isEqualTo(409);
        assertThat(problema.getDetail()).contains("alterada por outra pessoa");
    }
}
