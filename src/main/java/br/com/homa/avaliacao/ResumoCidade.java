package br.com.homa.avaliacao;

import java.util.List;

/** Média das percepções registradas para uma cidade. Não é um indicador oficial. */
public record ResumoCidade(
        String cidade,
        int totalAvaliacoes,
        Double custoDeVida,
        Double culturaLazer,
        Double mobilidade,
        Double espacosPublicos,
        Double convivencia,
        List<String> comentariosRecentes
) {
    public static ResumoCidade de(String cidade, List<Avaliacao> avaliacoes) {
        if (avaliacoes.isEmpty()) {
            return new ResumoCidade(cidade, 0, null, null, null, null, null, List.of());
        }
        return new ResumoCidade(
                cidade,
                avaliacoes.size(),
                media(avaliacoes.stream().mapToInt(Avaliacao::getCustoDeVida).average().orElse(0)),
                media(avaliacoes.stream().mapToInt(Avaliacao::getCulturaLazer).average().orElse(0)),
                media(avaliacoes.stream().mapToInt(Avaliacao::getMobilidade).average().orElse(0)),
                media(avaliacoes.stream().mapToInt(Avaliacao::getEspacosPublicos).average().orElse(0)),
                media(avaliacoes.stream().mapToInt(Avaliacao::getConvivencia).average().orElse(0)),
                avaliacoes.stream()
                        .map(Avaliacao::getComentario)
                        .filter(c -> c != null && !c.isBlank())
                        .limit(5)
                        .toList()
        );
    }

    private static Double media(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
