package br.com.homa;

import br.com.homa.avaliacao.AvaliacaoRepository;
import br.com.homa.dica.DicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"homa.dados-exemplo=false", "APP_ENV=teste"})
class HomaApiTest {

    @Autowired MockMvc mvc;
    @Autowired DicaRepository dicas;
    @Autowired AvaliacaoRepository avaliacoes;

    @BeforeEach
    void limpar() {
        dicas.deleteAll();
        avaliacoes.deleteAll();
    }

    private void criarDica(String cidade, String categoria, boolean gratuita) throws Exception {
        mvc.perform(post("/api/dicas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"cidade":"%s","bairro":"Centro","categoria":"%s","gratuita":%s,
                         "titulo":"Um prato feito honesto","descricao":"Comida caseira por um preço justo, perto do centro."}
                        """.formatted(cidade, categoria, gratuita)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Cadastra uma dica e consulta por cidade")
    void cadastraEConsultaDica() throws Exception {
        criarDica("Passo Fundo", "COMIDA", false);

        mvc.perform(get("/api/dicas").param("cidade", "passo fundo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Um prato feito honesto"))
                .andExpect(jsonPath("$[0].exemplo").value(false));
    }

    @Test
    @DisplayName("Filtra dicas por categoria e por gratuidade")
    void filtraDicas() throws Exception {
        criarDica("Passo Fundo", "COMIDA", false);
        criarDica("Passo Fundo", "CULTURA", true);
        criarDica("Porto Alegre", "CULTURA", true);

        mvc.perform(get("/api/dicas").param("cidade", "Passo Fundo").param("categoria", "CULTURA"))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/dicas").param("cidade", "Passo Fundo").param("gratuita", "true"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoria").value("CULTURA"));
    }

    @Test
    @DisplayName("Recusa dica sem título ou com link que não é do Google Maps")
    void validaDica() throws Exception {
        mvc.perform(post("/api/dicas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"cidade":"Passo Fundo","categoria":"COMIDA","titulo":"","descricao":"curta"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists());

        mvc.perform(post("/api/dicas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"cidade":"Passo Fundo","categoria":"COMIDA","titulo":"Café do bairro",
                         "descricao":"Um café tranquilo para ler.","mapsUrl":"https://exemplo.com/x"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.mapsUrl").exists());
    }

    @Test
    @DisplayName("Calcula a média das avaliações da cidade")
    void resumoAvaliacoes() throws Exception {
        String a1 = """
                {"cidade":"Passo Fundo","custoDeVida":2,"culturaLazer":3,"mobilidade":4,
                 "espacosPublicos":4,"convivencia":5,"comentario":"O aluguel é mais caro do que eu esperava."}
                """;
        String a2 = """
                {"cidade":"Passo Fundo","custoDeVida":3,"culturaLazer":4,"mobilidade":4,
                 "espacosPublicos":3,"convivencia":4}
                """;
        mvc.perform(post("/api/avaliacoes").contentType(MediaType.APPLICATION_JSON).content(a1))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/avaliacoes").contentType(MediaType.APPLICATION_JSON).content(a2))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/avaliacoes/resumo").param("cidade", "Passo Fundo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAvaliacoes").value(2))
                .andExpect(jsonPath("$.custoDeVida").value(2.5))
                .andExpect(jsonPath("$.espacosPublicos").value(3.5))
                .andExpect(jsonPath("$.comentariosRecentes", hasSize(1)));
    }

    @Test
    @DisplayName("Recusa nota fora da escala de 1 a 5")
    void validaAvaliacao() throws Exception {
        mvc.perform(post("/api/avaliacoes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"cidade":"Passo Fundo","custoDeVida":7,"culturaLazer":3,"mobilidade":4,
                         "espacosPublicos":4,"convivencia":5}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.custoDeVida").exists());
    }

    @Test
    @DisplayName("Informa o ambiente e responde ao health check")
    void infoEHealth() throws Exception {
        mvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambiente").value("teste"));
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Serve a interface web na raiz")
    void interfaceWeb() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}
