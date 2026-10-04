package info.bitsolucoes.solicitacoes.controller;

import info.bitsolucoes.solicitacoes.domain.StatusSolicitacao;
import info.bitsolucoes.solicitacoes.dto.AlterarStatusRequest;
import info.bitsolucoes.solicitacoes.dto.PaginaResponse;
import info.bitsolucoes.solicitacoes.dto.SolicitacaoRequest;
import info.bitsolucoes.solicitacoes.dto.SolicitacaoResponse;
import info.bitsolucoes.solicitacoes.repository.FiltroSolicitacao;
import info.bitsolucoes.solicitacoes.security.UsuarioAutenticado;
import info.bitsolucoes.solicitacoes.service.SolicitacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService service;

    public SolicitacaoController(SolicitacaoService service) {
        this.service = service;
    }

    @GetMapping
    public PaginaResponse<SolicitacaoResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Short categoriaId,
            @RequestParam(required = false) StatusSolicitacao status,
            @RequestParam(required = false) String titulo,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamanho,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        var filtro = new FiltroSolicitacao(dataInicio, dataFim, categoriaId, status, titulo);
        return service.listar(filtro, pagina, tamanho, usuario.getId());
    }

    @GetMapping("/{id}")
    public SolicitacaoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.buscar(id, usuario.getId());
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> criar(@Valid @RequestBody SolicitacaoRequest dados,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        SolicitacaoResponse criada = service.criar(dados, usuario.getId());
        return ResponseEntity.created(URI.create("/api/solicitacoes/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public SolicitacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody SolicitacaoRequest dados,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.atualizar(id, dados, usuario.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        service.excluir(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public SolicitacaoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusRequest dados,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.alterarStatus(id, dados.status(), usuario.getId());
    }
}
