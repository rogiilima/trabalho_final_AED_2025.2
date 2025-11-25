package br.com.icev.aed.service;

import br.com.icev.aed.entity.Trabalho;
import br.com.icev.aed.entity.TestesTrabalho;
import br.com.icev.aed.repository.TrabalhoRepository;
import br.com.icev.aed.repository.TestesTrabalhoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TesteTrabalhoService {

    private static final Logger logger = LoggerFactory.getLogger(TesteTrabalhoService.class);
    
    @Autowired
    private TrabalhoRepository trabalhoRepository;
    
    @Autowired
    private TestesTrabalhoRepository testesTrabalhoRepository;
    
    private static final String ARQUIVO_LOGS = "arquivo_logs.csv";
    
    /**
     * Testa um trabalho específico executando os 5 desafios
     */
    public String testarTrabalho(Long trabalhoId) {
        Optional<Trabalho> trabalhoOpt = trabalhoRepository.findById(trabalhoId);
        
        if (trabalhoOpt.isEmpty()) {
            return "Trabalho não encontrado";
        }
        
        Trabalho trabalho = trabalhoOpt.get();
        
        if (trabalho.getClasseQueImplementa() == null) {
            return "Classe de implementação não identificada";
        }
        
        // Cria registro de teste
        TestesTrabalho testeExecucao = new TestesTrabalho(trabalho, LocalDateTime.now());
        testeExecucao.setStatusExecucao("RUNNING");
        testeExecucao.setCategoria("TRABALHO_FORENSE_AED");
        testesTrabalhoRepository.save(testeExecucao);
        
        StringBuilder resultado = new StringBuilder();
        resultado.append("=== TESTE DO TRABALHO ===\n");
        resultado.append("ID: ").append(trabalho.getId()).append("\n");
        resultado.append("Alunos: ").append(getMatriculas(trabalho)).append("\n");
        resultado.append("Classe: ").append(trabalho.getClasseQueImplementa()).append("\n");
        resultado.append("JAR: ").append(trabalho.getCaminhoParaJar()).append("\n\n");
        
        try {
            // Carrega o JAR
            File jarFile = new File(trabalho.getCaminhoParaJar());
            if (!jarFile.exists()) {
                throw new Exception("Arquivo JAR não encontrado: " + trabalho.getCaminhoParaJar());
            }
            
            // Localiza o arquivo de logs
            String caminhoLogs = localizarArquivoLogs();
            if (caminhoLogs == null) {
                throw new Exception("Arquivo " + ARQUIVO_LOGS + " não encontrado");
            }
            
            resultado.append("Arquivo de logs: ").append(caminhoLogs).append("\n\n");
            
            // Cria ClassLoader para o JAR
            URL jarUrl = jarFile.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                this.getClass().getClassLoader()
            );
            
            // Carrega a classe
            Class<?> classe = classLoader.loadClass(trabalho.getClasseQueImplementa());
            Object instancia = classe.getDeclaredConstructor().newInstance();
            
            // Executa os 5 desafios
            int testesPassados = 0;
            int testesFalhados = 0;
            
            // Desafio 1: Encontrar Sessões Inválidas
            resultado.append("--- DESAFIO 1: Encontrar Sessões Inválidas ---\n");
            try {
                Method metodo = classe.getMethod("encontrarSessoesInvalidas", String.class);
                Set<?> sessoes = (Set<?>) metodo.invoke(instancia, caminhoLogs);
                resultado.append("✓ Executado com sucesso\n");
                resultado.append("  Sessões inválidas encontradas: ").append(sessoes.size()).append("\n");
                resultado.append("  Exemplos: ").append(limitarExemplos(sessoes, 5)).append("\n\n");
                testesPassados++;
            } catch (Exception e) {
                resultado.append("✗ FALHOU: ").append(e.getMessage()).append("\n\n");
                testesFalhados++;
            }
            
            // Desafio 2: Reconstruir Linha do Tempo
            resultado.append("--- DESAFIO 2: Reconstruir Linha do Tempo ---\n");
            try {
                Method metodo = classe.getMethod("reconstruirLinhaTempo", String.class, String.class);
                List<?> timeline = (List<?>) metodo.invoke(instancia, caminhoLogs, "SID12345");
                resultado.append("✓ Executado com sucesso\n");
                resultado.append("  Eventos na timeline: ").append(timeline.size()).append("\n");
                resultado.append("  Primeiros eventos: ").append(limitarExemplos(timeline, 10)).append("\n\n");
                testesPassados++;
            } catch (Exception e) {
                resultado.append("✗ FALHOU: ").append(e.getMessage()).append("\n\n");
                testesFalhados++;
            }
            
            // Desafio 3: Priorizar Alertas
            resultado.append("--- DESAFIO 3: Priorizar Alertas ---\n");
            try {
                Method metodo = classe.getMethod("priorizarAlertas", String.class, int.class);
                List<?> alertas = (List<?>) metodo.invoke(instancia, caminhoLogs, 5);
                resultado.append("✓ Executado com sucesso\n");
                resultado.append("  Top alertas retornados: ").append(alertas.size()).append("\n");
                resultado.append("  Alertas: ").append(alertas).append("\n\n");
                testesPassados++;
            } catch (Exception e) {
                resultado.append("✗ FALHOU: ").append(e.getMessage()).append("\n\n");
                testesFalhados++;
            }
            
            // Desafio 4: Encontrar Picos de Transferência
            resultado.append("--- DESAFIO 4: Encontrar Picos de Transferência ---\n");
            try {
                Method metodo = classe.getMethod("encontrarPicosTransferencia", String.class);
                Map<?, ?> picos = (Map<?, ?>) metodo.invoke(instancia, caminhoLogs);
                resultado.append("✓ Executado com sucesso\n");
                resultado.append("  Picos encontrados: ").append(picos.size()).append("\n");
                resultado.append("  Exemplos: ").append(limitarExemplos(picos.entrySet(), 5)).append("\n\n");
                testesPassados++;
            } catch (Exception e) {
                resultado.append("✗ FALHOU: ").append(e.getMessage()).append("\n\n");
                testesFalhados++;
            }
            
            // Desafio 5: Rastrear Contaminação
            resultado.append("--- DESAFIO 5: Rastrear Contaminação ---\n");
            try {
                Method metodo = classe.getMethod("rastrearContaminacao", String.class, String.class, String.class);
                Optional<?> caminho = (Optional<?>) metodo.invoke(instancia, caminhoLogs, "/home/user/docs", "/var/log/system.log");
                resultado.append("✓ Executado com sucesso\n");
                if (caminho.isPresent()) {
                    resultado.append("  Caminho encontrado: ").append(caminho.get()).append("\n\n");
                } else {
                    resultado.append("  Nenhum caminho encontrado\n\n");
                }
                testesPassados++;
            } catch (Exception e) {
                resultado.append("✗ FALHOU: ").append(e.getMessage()).append("\n\n");
                testesFalhados++;
            }
            
            classLoader.close();
            
            // Resumo
            resultado.append("=== RESUMO ===\n");
            resultado.append("Testes passados: ").append(testesPassados).append("/5\n");
            resultado.append("Testes falhados: ").append(testesFalhados).append("/5\n");
            
            String status = testesFalhados == 0 ? "SUCCESS" : "FAILED";
            
            // Atualiza o trabalho com os resultados
            trabalho.setTestado(true);
            trabalho.setDataTeste(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            trabalho.setResultadoTestes(resultado.toString());
            trabalho.setStatusTeste(status);
            trabalhoRepository.save(trabalho);
            
            // Atualiza o registro de teste
            testeExecucao.setHorarioFim(LocalDateTime.now());
            testeExecucao.setStatusExecucao("COMPLETED");
            testeExecucao.setQuantidadeTestesUnitarios(5); // 5 desafios
            testeExecucao.setResultado(resultado.toString());
            testesTrabalhoRepository.save(testeExecucao);
            
            logger.info("Trabalho {} testado. Status: {} ({}/5 testes passaram). Duração: {}s", 
                trabalhoId, status, testesPassados, testeExecucao.getDuracaoEmSegundos());
            
            return resultado.toString();
            
        } catch (Exception e) {
            String erro = "ERRO ao executar testes: " + e.getMessage();
            resultado.append("\n").append(erro).append("\n");
            
            trabalho.setTestado(true);
            trabalho.setDataTeste(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            trabalho.setResultadoTestes(resultado.toString());
            trabalho.setStatusTeste("ERROR");
            trabalhoRepository.save(trabalho);
            
            // Atualiza o registro de teste com erro
            testeExecucao.setHorarioFim(LocalDateTime.now());
            testeExecucao.setStatusExecucao("ERROR");
            testeExecucao.setResultado(resultado.toString());
            testesTrabalhoRepository.save(testeExecucao);
            
            logger.error("Erro ao testar trabalho {}: {}", trabalhoId, e.getMessage(), e);
            
            return resultado.toString();
        }
    }
    
    private String localizarArquivoLogs() {
        // Tenta vários locais possíveis
        String[] possiveisCaminhos = {
            ARQUIVO_LOGS,
            "../" + ARQUIVO_LOGS,
            "../../" + ARQUIVO_LOGS,
            System.getProperty("user.dir") + "/" + ARQUIVO_LOGS,
            System.getProperty("user.dir") + "/../" + ARQUIVO_LOGS
        };
        
        for (String caminho : possiveisCaminhos) {
            File file = new File(caminho);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
        
        return null;
    }
    
    private String getMatriculas(Trabalho trabalho) {
        List<String> matriculas = new ArrayList<>();
        if (trabalho.getMatriculaAluno1() != null) matriculas.add(trabalho.getMatriculaAluno1());
        if (trabalho.getMatriculaAluno2() != null) matriculas.add(trabalho.getMatriculaAluno2());
        if (trabalho.getMatriculaAluno3() != null) matriculas.add(trabalho.getMatriculaAluno3());
        return String.join(", ", matriculas);
    }
    
    private String limitarExemplos(Collection<?> colecao, int limite) {
        if (colecao == null || colecao.isEmpty()) {
            return "[]";
        }
        
        List<?> lista = new ArrayList<>(colecao);
        if (lista.size() <= limite) {
            return lista.toString();
        }
        
        return lista.subList(0, limite).toString() + " ... (+" + (lista.size() - limite) + " mais)";
    }
}
