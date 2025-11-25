package br.com.icev.aed.controller;

import br.com.icev.aed.entity.TestesTrabalho;
import br.com.icev.aed.repository.TestesTrabalhoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testes")
public class TestesTrabalhoController {
    
    @Autowired
    private TestesTrabalhoRepository testesTrabalhoRepository;
    
    // GET - Listar todos os testes
    @GetMapping
    public List<TestesTrabalho> getAllTestes() {
        return testesTrabalhoRepository.findAll();
    }
    
    // GET - Buscar testes de um trabalho específico
    @GetMapping("/trabalho/{trabalhoId}")
    public List<TestesTrabalho> getTestesByTrabalho(@PathVariable Long trabalhoId) {
        return testesTrabalhoRepository.findByTrabalhoId(trabalhoId);
    }
    
    // GET - Buscar testes por status
    @GetMapping("/status/{status}")
    public List<TestesTrabalho> getTestesByStatus(@PathVariable String status) {
        return testesTrabalhoRepository.findByStatusExecucao(status);
    }
    
    // GET - Buscar testes por categoria
    @GetMapping("/categoria/{categoria}")
    public List<TestesTrabalho> getTestesByCategoria(@PathVariable String categoria) {
        return testesTrabalhoRepository.findByCategoria(categoria);
    }
    
    // GET - Buscar teste específico
    @GetMapping("/{id}")
    public ResponseEntity<TestesTrabalho> getTesteById(@PathVariable Long id) {
        return testesTrabalhoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
