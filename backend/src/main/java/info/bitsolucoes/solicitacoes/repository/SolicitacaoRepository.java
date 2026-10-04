package info.bitsolucoes.solicitacoes.repository;

import info.bitsolucoes.solicitacoes.domain.Solicitacao;
import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SolicitacaoRepository
        extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {

    /** Carrega categoria e solicitante na mesma consulta da listagem (evita N+1). */
    @Override
    @EntityGraph(attributePaths = {"categoria", "solicitante"})
    Page<Solicitacao> findAll(Specification<Solicitacao> spec, Pageable pageable);

    /** Quantidade de solicitações por status (status sem registros não aparecem). */
    @Query("select s.status as status, count(s) as total from Solicitacao s group by s.status")
    List<ContagemPorStatus> contarPorStatus();

    interface ContagemPorStatus {
        StatusSolicitacao getStatus();

        long getTotal();
    }
}
