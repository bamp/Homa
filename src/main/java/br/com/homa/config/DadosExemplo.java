package br.com.homa.config;

import br.com.homa.dica.Categoria;
import br.com.homa.dica.Dica;
import br.com.homa.dica.DicaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carrega dicas de exemplo quando o banco está vazio, para a interface não abrir em branco.
 * Ficam marcadas como exemplo e aparecem depois das contribuições reais.
 */
@Component
public class DadosExemplo implements ApplicationRunner {

    private final DicaRepository dicas;
    private final boolean ativo;

    public DadosExemplo(DicaRepository dicas, @Value("${homa.dados-exemplo}") boolean ativo) {
        this.dicas = dicas;
        this.ativo = ativo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!ativo || dicas.count() > 0) {
            return;
        }
        dicas.saveAll(List.of(
                exemplo("Passo Fundo", "Centro", Categoria.COMIDA, false,
                        "O prato feito que salva a semana",
                        "Arroz, feijão, salada e uma carne bem feita por um preço justo. Vale perguntar o horário: muita coisa fecha cedo, inclusive no sábado."),
                exemplo("Passo Fundo", "Centro", Categoria.COMIDA, false,
                        "Onde tem opção sem glúten e vegetariana",
                        "Ainda são poucas na cidade. Esta dica serve para reunir os lugares que atendem quem tem restrição alimentar ou não come carne."),
                exemplo("Passo Fundo", null, Categoria.FAMILIA, true,
                        "Praça com sombra para levar as crianças",
                        "Antes de ir, repare em banheiro, bebedouro, acessibilidade e se dá para chegar a pé ou de ônibus."),
                exemplo("Passo Fundo", null, Categoria.CULTURA, true,
                        "Programação gratuita da semana",
                        "Muita coisa só aparece no Instagram, às vezes só no link da bio. Aqui a ideia é juntar o que vale a pena num lugar só."),
                exemplo("Porto Alegre", "Centro Histórico", Categoria.COMIDA, false,
                        "Laminuta às três da tarde de um sábado",
                        "Raro fora do shopping, mas existe. Bom para quem almoça fora do horário comercial."),
                exemplo("Porto Alegre", null, Categoria.ESPACOS, true,
                        "Parque para tomar mate no fim da tarde",
                        "Confira sombra, iluminação e como voltar de transporte público antes de escurecer."),
                exemplo("Porto Alegre", "Cidade Baixa", Categoria.CULTURA, true,
                        "Samba de graça no domingo",
                        "Confirme horário e local antes de sair, porque a programação muda.")
        ));
    }

    private static Dica exemplo(String cidade, String bairro, Categoria categoria, boolean gratuita,
                                String titulo, String descricao) {
        Dica d = new Dica();
        d.setCidade(cidade);
        d.setBairro(bairro);
        d.setCategoria(categoria);
        d.setGratuita(gratuita);
        d.setTitulo(titulo);
        d.setDescricao(descricao);
        d.setExemplo(true);
        return d;
    }
}
