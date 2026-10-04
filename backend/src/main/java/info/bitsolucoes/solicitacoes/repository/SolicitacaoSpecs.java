package info.bitsolucoes.solicitacoes.repository;

import info.bitsolucoes.solicitacoes.domain.Solicitacao;
import jakarta.persistence.criteria.Predicate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Monta a consulta dinâmica da listagem a partir do {@link FiltroSolicitacao}. */
public final class SolicitacaoSpecs {

    private static final char ESCAPE = '\\';

    private SolicitacaoSpecs() {
    }

    public static Specification<Solicitacao> comFiltro(FiltroSolicitacao filtro, ZoneId fuso) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();

            if (filtro.dataInicio() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.get("criadoEm"),
                        filtro.dataInicio().atStartOfDay(fuso).toOffsetDateTime()));
            }
            if (filtro.dataFim() != null) {
                // fim inclusivo: até o último instante do dia informado
                condicoes.add(cb.lessThan(root.get("criadoEm"),
                        filtro.dataFim().plusDays(1).atStartOfDay(fuso).toOffsetDateTime()));
            }
            if (filtro.categoriaId() != null) {
                condicoes.add(cb.equal(root.get("categoria").get("id"), filtro.categoriaId()));
            }
            if (filtro.status() != null) {
                condicoes.add(cb.equal(root.get("status"), filtro.status()));
            }
            if (filtro.titulo() != null && !filtro.titulo().isBlank()) {
                String termo = "%" + escapar(filtro.titulo().trim().toLowerCase()) + "%";
                condicoes.add(cb.like(cb.lower(root.get("titulo")), termo, ESCAPE));
            }
            return cb.and(condicoes.toArray(Predicate[]::new));
        };
    }

    /** Impede que % e _ digitados pelo usuário funcionem como curingas do LIKE. */
    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
