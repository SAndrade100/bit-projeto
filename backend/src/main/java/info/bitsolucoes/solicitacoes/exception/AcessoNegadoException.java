package info.bitsolucoes.solicitacoes.exception;

/** O usuário autenticado não tem permissão sobre o recurso (HTTP 403). */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
