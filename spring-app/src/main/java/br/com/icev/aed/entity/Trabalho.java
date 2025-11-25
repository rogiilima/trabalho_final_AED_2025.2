package br.com.icev.aed.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trabalhos")
public class Trabalho {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    @Column(name = "caminho_para_jar", nullable = false)
    private String caminhoParaJar;
    
    @Column(name = "matricula_aluno1", nullable = false)
    private String matriculaAluno1;
    
    @Column(name = "matricula_aluno2")
    private String matriculaAluno2;
    
    @Column(name = "matricula_aluno3")
    private String matriculaAluno3;
    
    @Column(name = "classe_que_implementa")
    private String classeQueImplementa;
    
    @Column(name = "testado")
    private Boolean testado = false;
    
    @Column(name = "data_teste")
    private String dataTeste;
    
    @Column(name = "resultado_testes", columnDefinition = "TEXT")
    private String resultadoTestes;
    
    @Column(name = "status_teste")
    private String statusTeste; // SUCCESS, FAILED, ERROR
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "data_entrega")
    private String dataEntrega;
    
    // Construtores
    public Trabalho() {}
    
    public Trabalho(String titulo, String caminhoParaJar, String matriculaAluno1) {
        this.titulo = titulo;
        this.caminhoParaJar = caminhoParaJar;
        this.matriculaAluno1 = matriculaAluno1;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getCaminhoParaJar() {
        return caminhoParaJar;
    }
    
    public void setCaminhoParaJar(String caminhoParaJar) {
        this.caminhoParaJar = caminhoParaJar;
    }
    
    public String getMatriculaAluno1() {
        return matriculaAluno1;
    }
    
    public void setMatriculaAluno1(String matriculaAluno1) {
        this.matriculaAluno1 = matriculaAluno1;
    }
    
    public String getMatriculaAluno2() {
        return matriculaAluno2;
    }
    
    public void setMatriculaAluno2(String matriculaAluno2) {
        this.matriculaAluno2 = matriculaAluno2;
    }
    
    public String getMatriculaAluno3() {
        return matriculaAluno3;
    }
    
    public void setMatriculaAluno3(String matriculaAluno3) {
        this.matriculaAluno3 = matriculaAluno3;
    }
    
    public String getClasseQueImplementa() {
        return classeQueImplementa;
    }
    
    public void setClasseQueImplementa(String classeQueImplementa) {
        this.classeQueImplementa = classeQueImplementa;
    }
    
    public Boolean getTestado() {
        return testado;
    }
    
    public void setTestado(Boolean testado) {
        this.testado = testado;
    }
    
    public String getDataTeste() {
        return dataTeste;
    }
    
    public void setDataTeste(String dataTeste) {
        this.dataTeste = dataTeste;
    }
    
    public String getResultadoTestes() {
        return resultadoTestes;
    }
    
    public void setResultadoTestes(String resultadoTestes) {
        this.resultadoTestes = resultadoTestes;
    }
    
    public String getStatusTeste() {
        return statusTeste;
    }
    
    public void setStatusTeste(String statusTeste) {
        this.statusTeste = statusTeste;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDataEntrega() {
        return dataEntrega;
    }
    
    public void setDataEntrega(String dataEntrega) {
        this.dataEntrega = dataEntrega;
    }
    
    @Override
    public String toString() {
        return "Trabalho{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", caminhoParaJar='" + caminhoParaJar + '\'' +
                ", matriculaAluno1='" + matriculaAluno1 + '\'' +
                ", matriculaAluno2='" + matriculaAluno2 + '\'' +
                ", matriculaAluno3='" + matriculaAluno3 + '\'' +
                ", classeQueImplementa='" + classeQueImplementa + '\'' +
                ", descricao='" + descricao + '\'' +
                ", dataEntrega='" + dataEntrega + '\'' +
                '}';
    }
}
