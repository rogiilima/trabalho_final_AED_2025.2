package com.example.demo.controller;

import com.example.demo.entity.Trabalho;
import com.example.demo.repository.TrabalhoRepository;
import com.example.demo.service.TrabalhoLoaderService;
import com.example.demo.service.TesteTrabalhoService;
import com.example.demo.service.TestesUnitariosService;
import com.example.demo.service.TestesEficienciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/trabalhos")
public class TrabalhoController {
    
    @Autowired
    private TrabalhoRepository trabalhoRepository;
    
    @Autowired
    private TrabalhoLoaderService trabalhoLoaderService;
    
    @Autowired
    private TesteTrabalhoService testeTrabalhoService;
    
    @Autowired
    private TestesUnitariosService testesUnitariosService;
    
    @Autowired
    private TestesEficienciaService testesEficienciaService;
    
    // GET - Listar todos os trabalhos
    @GetMapping
    public List<Trabalho> getAllTrabalhos() {
        return trabalhoRepository.findAll();
    }
    
    // GET - Buscar trabalho por ID
    @GetMapping("/{id}")
    public ResponseEntity<Trabalho> getTrabalhoById(@PathVariable Long id) {
        Optional<Trabalho> trabalho = trabalhoRepository.findById(id);
        return trabalho.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    // GET - Buscar trabalhos por título (busca parcial)
    @GetMapping("/titulo/{titulo}")
    public List<Trabalho> getTrabalhosByTitulo(@PathVariable String titulo) {
        return trabalhoRepository.findByTituloContainingIgnoreCase(titulo);
    }
    
    // GET - Buscar trabalhos por matrícula de aluno
    @GetMapping("/aluno/{matricula}")
    public ResponseEntity<List<Trabalho>> getTrabalhosByAluno(@PathVariable String matricula) {
        List<Trabalho> trabalhos1 = trabalhoRepository.findByMatriculaAluno1(matricula);
        List<Trabalho> trabalhos2 = trabalhoRepository.findByMatriculaAluno2(matricula);
        List<Trabalho> trabalhos3 = trabalhoRepository.findByMatriculaAluno3(matricula);
        
        // Combinar todas as listas
        trabalhos1.addAll(trabalhos2);
        trabalhos1.addAll(trabalhos3);
        
        return ResponseEntity.ok(trabalhos1);
    }
    
    // POST - Criar novo trabalho
    @PostMapping
    public ResponseEntity<?> createTrabalho(@RequestBody Trabalho trabalho) {
        // Validação: pelo menos um aluno é obrigatório
        if (trabalho.getMatriculaAluno1() == null || trabalho.getMatriculaAluno1().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("É obrigatório informar pelo menos a matrícula do aluno 1");
        }
        
        // Validação: título é obrigatório
        if (trabalho.getTitulo() == null || trabalho.getTitulo().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("O título do trabalho é obrigatório");
        }
        
        // Validação: caminho para JAR é obrigatório
        if (trabalho.getCaminhoParaJar() == null || trabalho.getCaminhoParaJar().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("O caminho para o arquivo JAR é obrigatório");
        }
        
        Trabalho savedTrabalho = trabalhoRepository.save(trabalho);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTrabalho);
    }
    
    // PUT - Atualizar trabalho
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrabalho(@PathVariable Long id, @RequestBody Trabalho trabalhoDetails) {
        Optional<Trabalho> trabalhoOptional = trabalhoRepository.findById(id);
        
        if (trabalhoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Trabalho trabalho = trabalhoOptional.get();
        
        // Atualizar campos
        if (trabalhoDetails.getTitulo() != null) {
            trabalho.setTitulo(trabalhoDetails.getTitulo());
        }
        if (trabalhoDetails.getCaminhoParaJar() != null) {
            trabalho.setCaminhoParaJar(trabalhoDetails.getCaminhoParaJar());
        }
        if (trabalhoDetails.getMatriculaAluno1() != null) {
            trabalho.setMatriculaAluno1(trabalhoDetails.getMatriculaAluno1());
        }
        if (trabalhoDetails.getMatriculaAluno2() != null) {
            trabalho.setMatriculaAluno2(trabalhoDetails.getMatriculaAluno2());
        }
        if (trabalhoDetails.getMatriculaAluno3() != null) {
            trabalho.setMatriculaAluno3(trabalhoDetails.getMatriculaAluno3());
        }
        if (trabalhoDetails.getDescricao() != null) {
            trabalho.setDescricao(trabalhoDetails.getDescricao());
        }
        if (trabalhoDetails.getDataEntrega() != null) {
            trabalho.setDataEntrega(trabalhoDetails.getDataEntrega());
        }
        
        Trabalho updatedTrabalho = trabalhoRepository.save(trabalho);
        return ResponseEntity.ok(updatedTrabalho);
    }
    
    // DELETE - Deletar trabalho
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrabalho(@PathVariable Long id) {
        if (!trabalhoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        trabalhoRepository.deleteById(id);
        return ResponseEntity.ok().body("Trabalho deletado com sucesso");
    }
    
    // DELETE - Deletar todos os trabalhos
    @DeleteMapping
    public ResponseEntity<?> deleteAllTrabalhos() {
        trabalhoRepository.deleteAll();
        return ResponseEntity.ok().body("Todos os trabalhos foram deletados");
    }
    
    // GET - Estatísticas
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long total = trabalhoRepository.count();
        return ResponseEntity.ok(new Object() {
            public final long totalTrabalhos = total;
            public final String mensagem = "Total de trabalhos cadastrados";
        });
    }
    
    // POST - Forçar carregamento de JARs
    @PostMapping("/carregar-jars")
    public ResponseEntity<?> carregarJars() {
        String resultado = trabalhoLoaderService.carregarTrabalhos();
        Map<String, String> response = new HashMap<>();
        response.put("mensagem", resultado);
        return ResponseEntity.ok(response);
    }
    
    // POST - Testar um trabalho específico
    @PostMapping("/{id}/testar")
    public ResponseEntity<?> testarTrabalho(@PathVariable Long id) {
        String resultado = testeTrabalhoService.testarTrabalho(id);
        Map<String, Object> response = new HashMap<>();
        response.put("resultado", resultado);
        
        // Busca o trabalho atualizado
        Optional<Trabalho> trabalho = trabalhoRepository.findById(id);
        if (trabalho.isPresent()) {
            response.put("status", trabalho.get().getStatusTeste());
            response.put("testado", trabalho.get().getTestado());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // POST - Testar todos os trabalhos
    @PostMapping("/testar-todos")
    public ResponseEntity<?> testarTodos() {
        List<Trabalho> trabalhos = trabalhoRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> resultados = new java.util.ArrayList<>();
        
        for (Trabalho trabalho : trabalhos) {
            testeTrabalhoService.testarTrabalho(trabalho.getId());
            
            // Recarrega o trabalho atualizado
            Trabalho atualizado = trabalhoRepository.findById(trabalho.getId()).get();
            Map<String, Object> info = new HashMap<>();
            info.put("id", atualizado.getId());
            info.put("titulo", atualizado.getTitulo());
            info.put("status", atualizado.getStatusTeste());
            info.put("testado", atualizado.getTestado());
            resultados.add(info);
        }
        
        response.put("total", trabalhos.size());
        response.put("resultados", resultados);
        return ResponseEntity.ok(response);
    }
    
    // POST - Executar testes unitários em um trabalho específico
    @PostMapping("/{id}/testar-unitarios")
    public ResponseEntity<?> testarUnitariosTrabalho(@PathVariable Long id) {
        try {
            Map<String, Object> resultado = testesUnitariosService.executarTestesUnitarios(id);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // POST - Executar testes unitários em todos os trabalhos
    @PostMapping("/testar-unitarios-todos")
    public ResponseEntity<?> testarUnitariosTodos() {
        List<Trabalho> trabalhos = trabalhoRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        for (Trabalho trabalho : trabalhos) {
            try {
                Map<String, Object> resultado = testesUnitariosService.executarTestesUnitarios(trabalho.getId());
                
                Map<String, Object> info = new HashMap<>();
                info.put("id", trabalho.getId());
                info.put("titulo", trabalho.getTitulo());
                info.put("pontuacao", resultado.get("pontuacao"));
                info.put("status", "SUCCESS");
                resultados.add(info);
            } catch (Exception e) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", trabalho.getId());
                info.put("titulo", trabalho.getTitulo());
                info.put("erro", e.getMessage());
                info.put("status", "ERROR");
                resultados.add(info);
            }
        }
        
        response.put("total", trabalhos.size());
        response.put("resultados", resultados);
        return ResponseEntity.ok(response);
    }
    
    // POST - Testar eficiência de um trabalho específico
    @PostMapping("/{id}/testar-eficiencia")
    public ResponseEntity<?> testarEficiencia(@PathVariable Long id) {
        try {
            Map<String, Object> resultado = testesEficienciaService.executarTestesEficiencia(id);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }
    
    // POST - Testar eficiência de todos os trabalhos
    @PostMapping("/testar-eficiencia-todos")
    public ResponseEntity<?> testarEficienciaTodos() {
        try {
            List<Map<String, Object>> resultados = testesEficienciaService.executarTestesEficienciaTodos();
            Map<String, Object> response = new HashMap<>();
            response.put("total", resultados.size());
            response.put("resultados", resultados);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }
}
