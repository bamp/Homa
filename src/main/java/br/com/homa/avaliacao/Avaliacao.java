package br.com.homa.avaliacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/** Percepção de um morador sobre a cidade, em cinco dimensões de qualidade de vida (1 a 5). */
@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String cidade;

    private int custoDeVida;
    private int culturaLazer;
    private int mobilidade;
    private int espacosPublicos;
    private int convivencia;

    @Column(length = 280)
    private String comentario;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public Long getId() { return id; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public int getCustoDeVida() { return custoDeVida; }
    public void setCustoDeVida(int v) { this.custoDeVida = v; }
    public int getCulturaLazer() { return culturaLazer; }
    public void setCulturaLazer(int v) { this.culturaLazer = v; }
    public int getMobilidade() { return mobilidade; }
    public void setMobilidade(int v) { this.mobilidade = v; }
    public int getEspacosPublicos() { return espacosPublicos; }
    public void setEspacosPublicos(int v) { this.espacosPublicos = v; }
    public int getConvivencia() { return convivencia; }
    public void setConvivencia(int v) { this.convivencia = v; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public Instant getCriadoEm() { return criadoEm; }
}
