package br.com.homa.dica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DicaRepository extends JpaRepository<Dica, Long> {

    /** Contribuições reais primeiro, depois os exemplos; mais recentes no topo. */
    @Query("select d from Dica d where lower(d.cidade) = lower(:cidade) order by d.exemplo asc, d.criadoEm desc")
    List<Dica> porCidade(@Param("cidade") String cidade);
}
