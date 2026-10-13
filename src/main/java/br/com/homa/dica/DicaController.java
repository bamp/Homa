package br.com.homa.dica;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/dicas")
public class DicaController {

    private final DicaRepository repository;

    public DicaController(DicaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Dica> listar(@RequestParam String cidade,
                             @RequestParam(required = false) Categoria categoria,
                             @RequestParam(required = false) String bairro,
                             @RequestParam(defaultValue = "false") boolean gratuita) {
        String filtroBairro = bairro == null ? "" : bairro.trim().toLowerCase(Locale.ROOT);
        return repository.porCidade(cidade.trim()).stream()
                .filter(d -> categoria == null || d.getCategoria() == categoria)
                .filter(d -> !gratuita || d.isGratuita())
                .filter(d -> filtroBairro.isEmpty()
                        || (d.getBairro() != null && d.getBairro().toLowerCase(Locale.ROOT).contains(filtroBairro)))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Dica criar(@Valid @RequestBody NovaDica nova) {
        return repository.save(nova.toEntity());
    }
}
