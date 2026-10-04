package info.bitsolucoes.solicitacoes.repository;

import info.bitsolucoes.solicitacoes.domain.Solicitacao;
import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SolicitacaoRepository
        extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {

    /** Quantidade de solicitações por status (status sem registros não aparecem). */
    @Query("select s.status as status, count(s) as total from Solicitacao s group by s.status")
    List<ContagemPorStatus> contarPorStatus();

    interface ContagemPorStatus {
        StatusSolicitacao getStatus();

        long getTotal();
    }
}
