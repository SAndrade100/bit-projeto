package info.bitsolucoes.solicitacoes.service;

import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import info.bitsolucoes.solicitacoes.dto.DashboardResponse;
import info.bitsolucoes.solicitacoes.repository.SolicitacaoRepository;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final SolicitacaoRepository solicitacoes;

    public DashboardService(SolicitacaoRepository solicitacoes) {
        this.solicitacoes = solicitacoes;
    }

    public DashboardResponse indicadores() {
        Map<StatusSolicitacao, Long> porStatus = new EnumMap<>(StatusSolicitacao.class);
        solicitacoes.contarPorStatus().forEach(c -> porStatus.put(c.getStatus(), c.getTotal()));

        long abertas = porStatus.getOrDefault(StatusSolicitacao.ABERTO, 0L);
        long emAtendimento = porStatus.getOrDefault(StatusSolicitacao.EM_ATENDIMENTO, 0L);
        long concluidas = porStatus.getOrDefault(StatusSolicitacao.CONCLUIDO, 0L);
        return new DashboardResponse(abertas + emAtendimento + concluidas, abertas, emAtendimento, concluidas);
    }
}
