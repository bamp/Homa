package br.com.homa.dica;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "dicas")
public class Dica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Column(length = 80)
    private String bairro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Column(nullable = false, length = 70)
    private String titulo;

    @Column(nullable = false, length = 280)
    private String descricao;

    @Column(nullable = false)
    private boolean gratuita;

    @Column(length = 300)
    private String mapsUrl;

    /** true para os exemplos carregados na inicialização (não são contribuições reais). */
    @Column(nullable = false)
    private boolean exemplo;

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
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public boolean isGratuita() { return gratuita; }
    public void setGratuita(boolean gratuita) { this.gratuita = gratuita; }
    public String getMapsUrl() { return mapsUrl; }
    public void setMapsUrl(String mapsUrl) { this.mapsUrl = mapsUrl; }
    public boolean isExemplo() { return exemplo; }
    public void setExemplo(boolean exemplo) { this.exemplo = exemplo; }
    public Instant getCriadoEm() { return criadoEm; }
}
