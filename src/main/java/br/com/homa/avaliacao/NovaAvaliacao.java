package br.com.homa.avaliacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovaAvaliacao(
        @NotBlank @Size(max = 80) String cidade,
        @Min(1) @Max(5) int custoDeVida,
        @Min(1) @Max(5) int culturaLazer,
        @Min(1) @Max(5) int mobilidade,
        @Min(1) @Max(5) int espacosPublicos,
        @Min(1) @Max(5) int convivencia,
        @Size(max = 280) String comentario
) {
    public Avaliacao toEntity() {
        Avaliacao a = new Avaliacao();
        a.setCidade(cidade.trim());
        a.setCustoDeVida(custoDeVida);
        a.setCulturaLazer(culturaLazer);
        a.setMobilidade(mobilidade);
        a.setEspacosPublicos(espacosPublicos);
        a.setConvivencia(convivencia);
        a.setComentario(comentario == null || comentario.isBlank() ? null : comentario.trim());
        return a;
    }
}
