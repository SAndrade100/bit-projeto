package info.bitsolucoes.solicitacoes.dto;

import info.bitsolucoes.solicitacoes.domain.Categoria;

public record CategoriaResponse(Short id, String nome) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
