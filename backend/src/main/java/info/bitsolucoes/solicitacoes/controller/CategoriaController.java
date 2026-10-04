package info.bitsolucoes.solicitacoes.controller;

import info.bitsolucoes.solicitacoes.dto.CategoriaResponse;
import info.bitsolucoes.solicitacoes.repository.CategoriaRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository categorias;

    public CategoriaController(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categorias.findAllByOrderByNomeAsc().stream().map(CategoriaResponse::de).toList();
    }
}
