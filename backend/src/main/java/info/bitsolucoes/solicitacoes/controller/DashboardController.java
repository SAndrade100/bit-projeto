package info.bitsolucoes.solicitacoes.controller;

import info.bitsolucoes.solicitacoes.dto.DashboardResponse;
import info.bitsolucoes.solicitacoes.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse indicadores() {
        return service.indicadores();
    }
}
