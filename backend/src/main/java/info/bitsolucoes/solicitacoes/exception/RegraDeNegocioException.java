package info.bitsolucoes.solicitacoes.exception;

/** A operação é válida, mas viola uma regra de negócio no estado atual (HTTP 409). */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
