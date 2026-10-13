package br.com.homa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Mostra em qual ambiente a aplicação está rodando e qual commit foi publicado.
 * O pipeline usa este endpoint para confirmar que o deploy chegou em staging e em produção.
 */
@RestController
public class InfoController {

    private final String ambiente;
    private final String commit;
    private final String versao;

    public InfoController(@Value("${homa.ambiente}") String ambiente,
                          @Value("${homa.commit}") String commit,
                          @Value("${homa.versao}") String versao) {
        this.ambiente = ambiente;
        this.commit = commit;
        this.versao = versao;
    }

    @GetMapping("/api/info")
    public Map<String, String> info() {
        return Map.of(
                "aplicacao", "homa",
                "ambiente", ambiente,
                "versao", versao,
                "commit", commit
        );
    }
}
