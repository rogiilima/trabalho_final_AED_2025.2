package br.com.icev.aed.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "testes_trabalho")
public class TestesTrabalho {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "trabalho_id", nullable = false)
    private Trabalho trabalho;
    
    @Column(name = "horario_inicio", nullable = false)
    private LocalDateTime horarioInicio;
    
    @Column(name = "horario_fim")
    private LocalDateTime horarioFim;
    
    @Column(name = "quantidade_testes_unitarios")
    private Integer quantidadeTestesUnitarios;
    
    @Column(name = "status_execucao")
    private String statusExecucao; // RUNNING, COMPLETED, FAILED, ERROR
    
    @Column(name = "categoria")
    private String categoria;
    
    @Column(name = "pontuacao")
    private Double pontuacao; // Média dos pontos dos desafios
    
    @Column(name = "resultado", columnDefinition = "TEXT")
    private String resultado;
    
    // Construtores
    public TestesTrabalho() {}
    
    public TestesTrabalho(Trabalho trabalho, LocalDateTime horarioInicio) {
        this.trabalho = trabalho;
        this.horarioInicio = horarioInicio;
        this.statusExecucao = "RUNNING";
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Trabalho getTrabalho() {
        return trabalho;
    }
    
    public void setTrabalho(Trabalho trabalho) {
        this.trabalho = trabalho;
    }
    
    public LocalDateTime getHorarioInicio() {
        return horarioInicio;
    }
    
    public void setHorarioInicio(LocalDateTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }
    
    public LocalDateTime getHorarioFim() {
        return horarioFim;
    }
    
    public void setHorarioFim(LocalDateTime horarioFim) {
        this.horarioFim = horarioFim;
    }
    
    public Integer getQuantidadeTestesUnitarios() {
        return quantidadeTestesUnitarios;
    }
    
    public void setQuantidadeTestesUnitarios(Integer quantidadeTestesUnitarios) {
        this.quantidadeTestesUnitarios = quantidadeTestesUnitarios;
    }
    
    public String getStatusExecucao() {
        return statusExecucao;
    }
    
    public void setStatusExecucao(String statusExecucao) {
        this.statusExecucao = statusExecucao;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public Double getPontuacao() {
        return pontuacao;
    }
    
    public void setPontuacao(Double pontuacao) {
        this.pontuacao = pontuacao;
    }
    
    public String getResultado() {
        return resultado;
    }
    
    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
    
    // Métodos auxiliares
    public Long getDuracaoEmSegundos() {
        if (horarioInicio != null && horarioFim != null) {
            return java.time.Duration.between(horarioInicio, horarioFim).getSeconds();
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "TestesTrabalho{" +
                "id=" + id +
                ", trabalho=" + (trabalho != null ? trabalho.getId() : null) +
                ", horarioInicio=" + horarioInicio +
                ", horarioFim=" + horarioFim +
                ", quantidadeTestesUnitarios=" + quantidadeTestesUnitarios +
                ", statusExecucao='" + statusExecucao + '\'' +
                ", categoria='" + categoria + '\'' +
                ", duracao=" + getDuracaoEmSegundos() + "s" +
                '}';
    }
}
