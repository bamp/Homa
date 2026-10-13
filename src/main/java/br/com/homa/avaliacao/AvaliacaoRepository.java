package br.com.homa.avaliacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    @Query("select a from Avaliacao a where lower(a.cidade) = lower(:cidade) order by a.criadoEm desc")
    List<Avaliacao> porCidade(@Param("cidade") String cidade);
}
