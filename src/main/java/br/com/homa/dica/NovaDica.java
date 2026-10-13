package br.com.homa.dica;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NovaDica(
        @NotBlank @Size(max = 80) String cidade,
        @Size(max = 80) String bairro,
        @NotNull Categoria categoria,
        @NotBlank @Size(min = 5, max = 70) String titulo,
        @NotBlank @Size(min = 12, max = 280) String descricao,
        boolean gratuita,
        @Size(max = 300)
        @Pattern(regexp = "^$|^https://(www\\.google\\.com/maps|maps\\.google\\.com|maps\\.app\\.goo\\.gl|goo\\.gl/maps)/.*$",
                message = "deve ser um link https do Google Maps")
        String mapsUrl
) {
    public Dica toEntity() {
        Dica d = new Dica();
        d.setCidade(cidade.trim());
        d.setBairro(bairro == null || bairro.isBlank() ? null : bairro.trim());
        d.setCategoria(categoria);
        d.setTitulo(titulo.trim());
        d.setDescricao(descricao.trim());
        d.setGratuita(gratuita);
        d.setMapsUrl(mapsUrl == null || mapsUrl.isBlank() ? null : mapsUrl.trim());
        d.setExemplo(false);
        return d;
    }
}
