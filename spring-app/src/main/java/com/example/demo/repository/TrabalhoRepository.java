package com.example.demo.repository;

import com.example.demo.entity.Trabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrabalhoRepository extends JpaRepository<Trabalho, Long> {
    
    List<Trabalho> findByMatriculaAluno1(String matricula);
    
    List<Trabalho> findByMatriculaAluno2(String matricula);
    
    List<Trabalho> findByMatriculaAluno3(String matricula);
    
    List<Trabalho> findByTituloContainingIgnoreCase(String titulo);
    
    Optional<Trabalho> findByCaminhoParaJar(String caminhoParaJar);
}
