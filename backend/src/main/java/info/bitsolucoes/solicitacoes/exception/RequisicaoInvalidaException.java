package info.bitsolucoes.solicitacoes.exception;

/** Os dados enviados são inválidos por motivo que a validação declarativa não cobre (HTTP 400). */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
