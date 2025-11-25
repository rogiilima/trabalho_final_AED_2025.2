package br.com.icev.aed.service;

import br.edu.icev.aed.forense.Alerta;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;
import br.com.icev.aed.entity.TestesTrabalho;
import br.com.icev.aed.entity.Trabalho;
import br.com.icev.aed.repository.TestesTrabalhoRepository;
import br.com.icev.aed.repository.TrabalhoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class TestesUnitariosService {
    
    private static final Logger logger = LoggerFactory.getLogger(TestesUnitariosService.class);
    private static final String ARQUIVO_LOGS = "arquivo_logs.csv";
    
    // Timeout de 30 segundos por desafio para evitar loops infinitos
    private static final int TIMEOUT_SEGUNDOS = 30;
    
    @Autowired
    private TrabalhoRepository trabalhoRepository;
    
    @Autowired
    private TestesTrabalhoRepository testesTrabalhoRepository;
    
    /**
     * Executa um teste com timeout para proteger contra loops infinitos ou travamentos
     */
    private <T> T executarComTimeout(Callable<T> tarefa, String nomeDesafio) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(tarefa);
        
        try {
            return future.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new Exception("TIMEOUT: " + nomeDesafio + " excedeu " + TIMEOUT_SEGUNDOS + " segundos (possível loop infinito)");
        } catch (ExecutionException e) {
            Throwable causa = e.getCause();
            if (causa instanceof Exception) {
                throw (Exception) causa;
            }
            throw new Exception("Erro ao executar " + nomeDesafio + ": " + causa.getMessage());
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new Exception("Execução de " + nomeDesafio + " foi interrompida");
        } finally {
            executor.shutdownNow();
        }
    }
    
    /**
     * Executa 50 testes unitários (10 por desafio) em um trabalho específico
     */
    public Map<String, Object> executarTestesUnitarios(Long trabalhoId) {
        logger.info("Iniciando testes unitários para trabalho ID: {}", trabalhoId);
        
        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));
        
        // Cria registro de teste
        TestesTrabalho testeExecucao = new TestesTrabalho(trabalho, LocalDateTime.now());
        testeExecucao.setStatusExecucao("RUNNING");
        testeExecucao.setCategoria("TESTE_UNITARIO");
        testeExecucao.setQuantidadeTestesUnitarios(50);
        testesTrabalhoRepository.save(testeExecucao);
        
        StringBuilder resultado = new StringBuilder();
        resultado.append("=== TESTES UNITÁRIOS ===\n");
        resultado.append("Trabalho ID: ").append(trabalho.getId()).append("\n");
        resultado.append("Alunos: ").append(getMatriculas(trabalho)).append("\n");
        resultado.append("Classe: ").append(trabalho.getClasseQueImplementa()).append("\n\n");
        
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
            
            // Cria ClassLoader para o JAR
            URL jarUrl = jarFile.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                this.getClass().getClassLoader()
            );
            
            // Carrega a classe
            Class<?> classe = Class.forName(trabalho.getClasseQueImplementa(), true, classLoader);
            Object instancia = classe.getDeclaredConstructor().newInstance();
            
            // Executa os testes para cada desafio
            Map<String, Double> pontosPorDesafio = new HashMap<>();
            
            // Desafio 1 - Com timeout
            try {
                Object instanciaFinal = instancia;
                Class<?> classeFinal = classe;
                String logsPathFinal = caminhoLogs;
                StringBuilder resultadoFinal = resultado;
                
                double pontos = executarComTimeout(() -> 
                    testarDesafio1(instanciaFinal, classeFinal, logsPathFinal, resultadoFinal), 
                    "Desafio 1"
                );
                pontosPorDesafio.put("Desafio 1", pontos);
            } catch (Exception e) {
                resultado.append("=== DESAFIO 1: ERRO ===\n");
                resultado.append("Erro: ").append(e.getMessage()).append("\n");
                if (e.getMessage().contains("TIMEOUT")) {
                    resultado.append("⚠️  O código do aluno pode ter um loop infinito ou está muito lento.\n");
                }
                resultado.append("\n");
                pontosPorDesafio.put("Desafio 1", 0.0);
                logger.error("Erro no Desafio 1: {}", e.getMessage());
            }
            
            // Desafio 2 - Com timeout
            try {
                Object instanciaFinal = instancia;
                Class<?> classeFinal = classe;
                String logsPathFinal = caminhoLogs;
                StringBuilder resultadoFinal = resultado;
                
                double pontos = executarComTimeout(() -> 
                    testarDesafio2(instanciaFinal, classeFinal, logsPathFinal, resultadoFinal), 
                    "Desafio 2"
                );
                pontosPorDesafio.put("Desafio 2", pontos);
            } catch (Exception e) {
                resultado.append("=== DESAFIO 2: ERRO ===\n");
                resultado.append("Erro: ").append(e.getMessage()).append("\n");
                if (e.getMessage().contains("TIMEOUT")) {
                    resultado.append("⚠️  O código do aluno pode ter um loop infinito ou está muito lento.\n");
                }
                resultado.append("\n");
                pontosPorDesafio.put("Desafio 2", 0.0);
                logger.error("Erro no Desafio 2: {}", e.getMessage());
            }
            
            // Desafio 3 - Com timeout
            try {
                Object instanciaFinal = instancia;
                Class<?> classeFinal = classe;
                String logsPathFinal = caminhoLogs;
                StringBuilder resultadoFinal = resultado;
                
                double pontos = executarComTimeout(() -> 
                    testarDesafio3(instanciaFinal, classeFinal, logsPathFinal, resultadoFinal), 
                    "Desafio 3"
                );
                pontosPorDesafio.put("Desafio 3", pontos);
            } catch (Exception e) {
                resultado.append("=== DESAFIO 3: ERRO ===\n");
                resultado.append("Erro: ").append(e.getMessage()).append("\n");
                if (e.getMessage().contains("TIMEOUT")) {
                    resultado.append("⚠️  O código do aluno pode ter um loop infinito ou está muito lento.\n");
                }
                resultado.append("\n");
                pontosPorDesafio.put("Desafio 3", 0.0);
                logger.error("Erro no Desafio 3: {}", e.getMessage());
            }
            
            // Desafio 4 - Com timeout
            try {
                Object instanciaFinal = instancia;
                Class<?> classeFinal = classe;
                String logsPathFinal = caminhoLogs;
                StringBuilder resultadoFinal = resultado;
                
                double pontos = executarComTimeout(() -> 
                    testarDesafio4(instanciaFinal, classeFinal, logsPathFinal, resultadoFinal), 
                    "Desafio 4"
                );
                pontosPorDesafio.put("Desafio 4", pontos);
            } catch (Exception e) {
                resultado.append("=== DESAFIO 4: ERRO ===\n");
                resultado.append("Erro: ").append(e.getMessage()).append("\n");
                if (e.getMessage().contains("TIMEOUT")) {
                    resultado.append("⚠️  O código do aluno pode ter um loop infinito ou está muito lento.\n");
                }
                resultado.append("\n");
                pontosPorDesafio.put("Desafio 4", 0.0);
                logger.error("Erro no Desafio 4: {}", e.getMessage());
            }
            
            // Desafio 5 - Com timeout
            try {
                Object instanciaFinal = instancia;
                Class<?> classeFinal = classe;
                String logsPathFinal = caminhoLogs;
                StringBuilder resultadoFinal = resultado;
                
                double pontos = executarComTimeout(() -> 
                    testarDesafio5(instanciaFinal, classeFinal, logsPathFinal, resultadoFinal), 
                    "Desafio 5"
                );
                pontosPorDesafio.put("Desafio 5", pontos);
            } catch (Exception e) {
                resultado.append("=== DESAFIO 5: ERRO ===\n");
                resultado.append("Erro: ").append(e.getMessage()).append("\n");
                if (e.getMessage().contains("TIMEOUT")) {
                    resultado.append("⚠️  O código do aluno pode ter um loop infinito ou está muito lento.\n");
                }
                resultado.append("\n");
                pontosPorDesafio.put("Desafio 5", 0.0);
                logger.error("Erro no Desafio 5: {}", e.getMessage());
            }
            
            // Calcula soma total dos pontos (máximo 70 pontos)
            double pontuacaoTotal = pontosPorDesafio.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
            
            // Adiciona resumo
            resultado.append("\n=== RESUMO DE PONTUAÇÃO ===\n");
            pontosPorDesafio.forEach((desafio, pontos) -> 
                resultado.append(String.format("%s: %.2f pontos\n", desafio, pontos))
            );
            resultado.append(String.format("\nPONTUAÇÃO TOTAL: %.2f/70.00 pontos\n", pontuacaoTotal));
            
            // Atualiza o teste
            testeExecucao.setHorarioFim(LocalDateTime.now());
            testeExecucao.setStatusExecucao("COMPLETED");
            testeExecucao.setResultado(resultado.toString());
            testeExecucao.setPontuacao(pontuacaoTotal);
            testesTrabalhoRepository.save(testeExecucao);
            
            // Atualiza trabalho
            trabalho.setTestado(true);
            trabalho.setDataTeste(LocalDateTime.now().toString());
            trabalhoRepository.save(trabalho);
            
            logger.info("Testes unitários concluídos. Pontuação: {:.2f}/70.00", pontuacaoTotal);
            
            Map<String, Object> response = new HashMap<>();
            response.put("resultado", resultado.toString());
            response.put("pontuacao", pontuacaoTotal);
            response.put("pontosPorDesafio", pontosPorDesafio);
            return response;
            
        } catch (OutOfMemoryError e) {
            // Protege contra estouro de memória
            String msgErro = "ERRO CRÍTICO: O código do aluno causou estouro de memória (OutOfMemoryError). " +
                           "Isso pode indicar criação excessiva de objetos ou estruturas de dados muito grandes.";
            
            testeExecucao.setHorarioFim(LocalDateTime.now());
            testeExecucao.setStatusExecucao("CRITICAL_ERROR");
            testeExecucao.setResultado(msgErro);
            testeExecucao.setPontuacao(0.0);
            testesTrabalhoRepository.save(testeExecucao);
            
            logger.error("OutOfMemoryError ao executar testes: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("resultado", msgErro);
            response.put("pontuacao", 0.0);
            response.put("erro", "OutOfMemoryError");
            return response;
            
        } catch (StackOverflowError e) {
            // Protege contra stack overflow (recursão infinita)
            String msgErro = "ERRO CRÍTICO: O código do aluno causou estouro de pilha (StackOverflowError). " +
                           "Isso geralmente indica recursão infinita ou recursão muito profunda.";
            
            testeExecucao.setHorarioFim(LocalDateTime.now());
            testeExecucao.setStatusExecucao("CRITICAL_ERROR");
            testeExecucao.setResultado(msgErro);
            testeExecucao.setPontuacao(0.0);
            testesTrabalhoRepository.save(testeExecucao);
            
            logger.error("StackOverflowError ao executar testes: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("resultado", msgErro);
            response.put("pontuacao", 0.0);
            response.put("erro", "StackOverflowError");
            return response;
            
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // Classe ou método não encontrado
            String msgErro = "ERRO: A classe ou método esperado não foi encontrado no JAR do aluno. " +
                           "Verifique se a classe implementa a interface AnaliseForenseAvancada corretamente. " +
                           "Detalhes: " + e.getMessage();
            
            testeExecucao.setHorarioFim(LocalDateTime.now());
            testeExecucao.setStatusExecucao("ERROR");
            testeExecucao.setResultado(msgErro);
            testeExecucao.setPontuacao(0.0);
            testesTrabalhoRepository.save(testeExecucao);
            
            logger.error("Classe/Método não encontrado: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("resultado", msgErro);
            response.put("pontuacao", 0.0);
            response.put("erro", "ClassNotFoundException/NoSuchMethodException");
            return response;
            
        } catch (Exception e) {
            // Qualquer outra exceção
            String msgErro = "ERRO: Falha ao executar os testes. " +
                           "Isso pode ser causado por: JAR corrompido, dependências faltando, " +
                           "exceção não tratada no código do aluno. Detalhes: " + e.getMessage();
            
            testeExecucao.setHorarioFim(LocalDateTime.now());
            testeExecucao.setStatusExecucao("ERROR");
            testeExecucao.setResultado(msgErro);
            testeExecucao.setPontuacao(0.0);
            testesTrabalhoRepository.save(testeExecucao);
            
            logger.error("Erro ao executar testes unitários: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("resultado", msgErro);
            response.put("pontuacao", 0.0);
            response.put("erro", e.getClass().getSimpleName());
            return response;
        }
    }
    
    /**
     * Desafio 1: Encontrar Sessões Inválidas (10 testes)
     */
    private double testarDesafio1(Object instancia, Class<?> classe, String caminhoLogs, StringBuilder resultado) throws Exception {
        resultado.append("=== DESAFIO 1: Encontrar Sessões Inválidas (10 testes) ===\n");
        
        Method metodo = classe.getMethod("encontrarSessoesInvalidas", String.class);
        Set<String> sessoesInvalidas = (Set<String>) metodo.invoke(instancia, caminhoLogs);
        
        int testesPassados = 0;
        
        // Teste 1: Resultado não é nulo
        if (sessoesInvalidas != null) {
            resultado.append("✓ Teste 1.1: Retornou resultado não-nulo\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 1.1: Retornou null\n");
            return calcularPontos(testesPassados, 10);
        }
        
        // Teste 2: Resultado é um Set
        if (sessoesInvalidas instanceof Set) {
            resultado.append("✓ Teste 1.2: Retornou um Set\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 1.2: Não retornou um Set\n");
        }
        
        // Teste 3: Encontrou sessões inválidas
        if (!sessoesInvalidas.isEmpty()) {
            resultado.append("✓ Teste 1.3: Encontrou sessões inválidas (" + sessoesInvalidas.size() + ")\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 1.3: Não encontrou sessões inválidas\n");
        }
        
        // Teste 4: Quantidade esperada (aproximadamente 23 sessões inválidas)
        if (sessoesInvalidas.size() >= 20 && sessoesInvalidas.size() <= 26) {
            resultado.append("✓ Teste 1.4: Quantidade na faixa esperada (20-26)\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 1.4: Quantidade fora da faixa esperada: " + sessoesInvalidas.size() + "\n");
        }
        
        // Teste 5: Verifica se contém sessões do tipo session-m (Mallory)
        long sessoesM = sessoesInvalidas.stream().filter(s -> s.startsWith("session-m")).count();
        if (sessoesM > 0) {
            resultado.append("✓ Teste 1.5: Detectou sessões inválidas de Mallory (" + sessoesM + ")\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 1.5: Não detectou sessões de Mallory\n");
        }
        
        // Teste 6: Não inclui sessões válidas de Alice
        long sessoesA = sessoesInvalidas.stream().filter(s -> s.startsWith("session-a")).count();
        if (sessoesA == 0) {
            resultado.append("✓ Teste 1.6: Não incluiu sessões válidas de Alice\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 1.6: Incluiu sessões de Alice incorretamente (" + sessoesA + ")\n");
        }
        
        // Teste 7: Verifica formato das sessões (session-X-NN)
        boolean formatoCorreto = sessoesInvalidas.stream()
                .allMatch(s -> s.matches("session-[a-z]-\\d{2}"));
        if (formatoCorreto) {
            resultado.append("✓ Teste 1.7: Todas as sessões têm formato correto\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 1.7: Algumas sessões têm formato incorreto\n");
        }
        
        // Teste 8: Não há duplicatas
        Set<String> semDuplicatas = new HashSet<>(sessoesInvalidas);
        if (semDuplicatas.size() == sessoesInvalidas.size()) {
            resultado.append("✓ Teste 1.8: Não há sessões duplicadas\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 1.8: Existem sessões duplicadas\n");
        }
        
        // Teste 9: Verifica se detectou sessão específica conhecida (session-m-10)
        if (sessoesInvalidas.contains("session-m-10")) {
            resultado.append("✓ Teste 1.9: Detectou sessão inválida conhecida (session-m-10)\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 1.9: Não detectou session-m-10\n");
        }
        
        // Teste 10: Eficiência - executou em tempo razoável (já executado, então passou)
        resultado.append("✓ Teste 1.10: Executou em tempo razoável\n");
        testesPassados++;
        
        double pontos = calcularPontos(testesPassados, 10);
        resultado.append(String.format("Pontuação Desafio 1: %.2f/14.0 (%d/10 testes)\n\n", pontos, testesPassados));
        return pontos;
    }
    
    /**
     * Desafio 2: Reconstruir Linha do Tempo (10 testes)
     */
    private double testarDesafio2(Object instancia, Class<?> classe, String caminhoLogs, StringBuilder resultado) throws Exception {
        resultado.append("=== DESAFIO 2: Reconstruir Linha do Tempo (10 testes) ===\n");
        
        Method metodo = classe.getMethod("reconstruirLinhaTempo", String.class, String.class);
        List<?> timeline = (List<?>) metodo.invoke(instancia, caminhoLogs, "alice");
        
        int testesPassados = 0;
        
        // Teste 1: Resultado não é nulo
        if (timeline != null) {
            resultado.append("✓ Teste 2.1: Retornou resultado não-nulo\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 2.1: Retornou null\n");
            return calcularPontos(testesPassados, 10);
        }
        
        // Teste 2: Resultado é uma List
        if (timeline instanceof List) {
            resultado.append("✓ Teste 2.2: Retornou uma List\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 2.2: Não retornou uma List\n");
        }
        
        // Teste 3: Timeline de Alice não está vazia
        if (!timeline.isEmpty()) {
            resultado.append("✓ Teste 2.3: Timeline de Alice não está vazia (" + timeline.size() + " eventos)\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 2.3: Timeline de Alice está vazia\n");
        }
        
        // Teste 4: Verifica ordenação por timestamp
        boolean ordenado = true;
        for (int i = 1; i < timeline.size(); i++) {
            String atual = timeline.get(i).toString();
            String anterior = timeline.get(i-1).toString();
            // Assume formato com timestamp no início
            if (atual.compareTo(anterior) < 0) {
                ordenado = false;
                break;
            }
        }
        if (ordenado) {
            resultado.append("✓ Teste 2.4: Eventos ordenados cronologicamente\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 2.4: Eventos fora de ordem\n");
        }
        
        // Teste 5: Testa com usuário Bob
        List<?> timelineBob = (List<?>) metodo.invoke(instancia, caminhoLogs, "bob");
        if (timelineBob != null && !timelineBob.isEmpty()) {
            resultado.append("✓ Teste 2.5: Timeline de Bob funciona (" + timelineBob.size() + " eventos)\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 2.5: Timeline de Bob vazia ou null\n");
        }
        
        // Teste 6: Testa com usuário inexistente
        List<?> timelineInexistente = (List<?>) metodo.invoke(instancia, caminhoLogs, "usuario_inexistente");
        if (timelineInexistente != null && timelineInexistente.isEmpty()) {
            resultado.append("✓ Teste 2.6: Retorna lista vazia para usuário inexistente\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 2.6: Comportamento incorreto para usuário inexistente\n");
        }
        
        // Teste 7: Verifica formato dos eventos (contém timestamp)
        boolean temTimestamp = !timeline.isEmpty() && 
                timeline.get(0).toString().matches(".*\\d{10}.*");
        if (temTimestamp) {
            resultado.append("✓ Teste 2.7: Eventos contêm timestamp\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 2.7: Eventos não contêm timestamp\n");
        }
        
        // Teste 8: Quantidade razoável de eventos para Alice (> 50)
        if (timeline.size() > 50) {
            resultado.append("✓ Teste 2.8: Alice tem quantidade razoável de eventos\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 2.8: Poucos eventos para Alice: " + timeline.size() + "\n");
        }
        
        // Teste 9: Verifica case-insensitive (ALICE vs alice)
        List<?> timelineUpper = (List<?>) metodo.invoke(instancia, caminhoLogs, "ALICE");
        if (timelineUpper != null && timelineUpper.size() == timeline.size()) {
            resultado.append("✓ Teste 2.9: Busca é case-insensitive\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 2.9: Busca não é case-insensitive\n");
        }
        
        // Teste 10: Executou com sucesso
        resultado.append("✓ Teste 2.10: Método executou sem exceções\n");
        testesPassados++;
        
        double pontos = calcularPontos(testesPassados, 10);
        resultado.append(String.format("Pontuação Desafio 2: %.2f/14.0 (%d/10 testes)\n\n", pontos, testesPassados));
        return pontos;
    }
    
    /**
     * Desafio 3: Priorizar Alertas (10 testes)
     */
    private double testarDesafio3(Object instancia, Class<?> classe, String caminhoLogs, StringBuilder resultado) throws Exception {
        resultado.append("=== DESAFIO 3: Priorizar Alertas (10 testes) ===\n");
        
        Method metodo = classe.getMethod("priorizarAlertas", String.class, int.class);
        int testesPassados = 0;
        
        // Teste 1: Top 5 alertas
        List<Alerta> top5 = (List<Alerta>) metodo.invoke(instancia, caminhoLogs, 5);
        if (top5 != null) {
            resultado.append("✓ Teste 3.1: Retornou resultado não-nulo para top 5\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.1: Retornou null\n");
            return calcularPontos(testesPassados, 10);
        }
        
        // Teste 2: Tamanho correto (5)
        if (top5.size() == 5) {
            resultado.append("✓ Teste 3.2: Retornou exatamente 5 alertas\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.2: Retornou " + top5.size() + " alertas ao invés de 5\n");
        }
        
        // Teste 3: Ordenação por severidade (decrescente)
        boolean ordenadoPorSeveridade = true;
        for (int i = 1; i < top5.size(); i++) {
            if (top5.get(i-1).getSeverityLevel() < top5.get(i).getSeverityLevel()) {
                ordenadoPorSeveridade = false;
                break;
            }
        }
        if (ordenadoPorSeveridade) {
            resultado.append("✓ Teste 3.3: Alertas ordenados por severidade (decrescente)\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.3: Alertas não ordenados corretamente\n");
        }
        
        // Teste 4: Primeiro alerta tem severidade máxima
        if (!top5.isEmpty() && top5.get(0).getSeverityLevel() >= 9) {
            resultado.append("✓ Teste 3.4: Primeiro alerta tem alta severidade (" + 
                           top5.get(0).getSeverityLevel() + ")\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.4: Primeiro alerta não tem severidade alta\n");
        }
        
        // Teste 5: Top 10 alertas
        List<Alerta> top10 = (List<Alerta>) metodo.invoke(instancia, caminhoLogs, 10);
        if (top10 != null && top10.size() == 10) {
            resultado.append("✓ Teste 3.5: Retornou 10 alertas corretamente\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.5: Falhou ao retornar 10 alertas\n");
        }
        
        // Teste 6: Top 1 alerta
        List<Alerta> top1 = (List<Alerta>) metodo.invoke(instancia, caminhoLogs, 1);
        if (top1 != null && top1.size() == 1) {
            resultado.append("✓ Teste 3.6: Retornou 1 alerta corretamente\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.6: Falhou ao retornar 1 alerta\n");
        }
        
        // Teste 7: Alertas contêm informações completas
        boolean temInformacoesCompletas = top5.stream().allMatch(a -> 
            a.getUserId() != null && !a.getUserId().isEmpty() &&
            a.getActionType() != null && !a.getActionType().isEmpty()
        );
        if (temInformacoesCompletas) {
            resultado.append("✓ Teste 3.7: Alertas contêm informações completas\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.7: Alertas com informações incompletas\n");
        }
        
        // Teste 8: Alertas de alta severidade são de usuários suspeitos
        long alertasEveMallory = top5.stream()
                .filter(a -> a.getUserId().equalsIgnoreCase("eve") || 
                           a.getUserId().equalsIgnoreCase("mallory"))
                .count();
        if (alertasEveMallory >= 3) {
            resultado.append("✓ Teste 3.8: Alertas de alta severidade são de usuários suspeitos\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.8: Poucos alertas de usuários suspeitos no top 5\n");
        }
        
        // Teste 9: Tipos de ação críticos
        long acoesFile = top5.stream()
                .filter(a -> a.getActionType().contains("FILE"))
                .count();
        if (acoesFile > 0) {
            resultado.append("✓ Teste 3.9: Detectou ações críticas de arquivo\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.9: Não detectou ações de arquivo críticas\n");
        }
        
        // Teste 10: Top 100 (teste de robustez)
        List<Alerta> top100 = (List<Alerta>) metodo.invoke(instancia, caminhoLogs, 100);
        if (top100 != null && top100.size() > 50) {
            resultado.append("✓ Teste 3.10: Método robusto para grandes quantidades\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 3.10: Problemas com grandes quantidades\n");
        }
        
        double pontos = calcularPontos(testesPassados, 10);
        resultado.append(String.format("Pontuação Desafio 3: %.2f/14.0 (%d/10 testes)\n\n", pontos, testesPassados));
        return pontos;
    }
    
    /**
     * Desafio 4: Encontrar Picos de Transferência (10 testes)
     */
    private double testarDesafio4(Object instancia, Class<?> classe, String caminhoLogs, StringBuilder resultado) throws Exception {
        resultado.append("=== DESAFIO 4: Encontrar Picos de Transferência (10 testes) ===\n");
        
        Method metodo = classe.getMethod("encontrarPicosTransferencia", String.class);
        Map<Long, Long> picos = (Map<Long, Long>) metodo.invoke(instancia, caminhoLogs);
        
        int testesPassados = 0;
        
        // Teste 1: Resultado não é nulo
        if (picos != null) {
            resultado.append("✓ Teste 4.1: Retornou resultado não-nulo\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 4.1: Retornou null\n");
            return calcularPontos(testesPassados, 10);
        }
        
        // Teste 2: É um Map
        if (picos instanceof Map) {
            resultado.append("✓ Teste 4.2: Retornou um Map\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 4.2: Não retornou um Map\n");
        }
        
        // Teste 3: Encontrou picos
        if (!picos.isEmpty()) {
            resultado.append("✓ Teste 4.3: Encontrou picos de transferência (" + picos.size() + ")\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 4.3: Não encontrou picos\n");
        }
        
        // Teste 4: Quantidade razoável de picos (entre 100 e 300)
        if (picos.size() >= 100 && picos.size() <= 300) {
            resultado.append("✓ Teste 4.4: Quantidade de picos na faixa esperada\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 4.4: Quantidade fora da faixa: " + picos.size() + "\n");
        }
        
        // Teste 5: Timestamps válidos (formato Unix epoch)
        boolean timestampsValidos = picos.keySet().stream()
                .allMatch(t -> t >= 1700000000L && t <= 1700999999L);
        if (timestampsValidos) {
            resultado.append("✓ Teste 4.5: Todos os timestamps são válidos\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 4.5: Alguns timestamps inválidos\n");
        }
        
        // Teste 6: Valores de próximo pico válidos
        boolean valoresValidos = picos.values().stream()
                .allMatch(v -> v >= 1700000000L && v <= 1700999999L);
        if (valoresValidos) {
            resultado.append("✓ Teste 4.6: Todos os valores são timestamps válidos\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 4.6: Alguns valores inválidos\n");
        }
        
        // Teste 7: Relação correta (próximo pico > pico atual)
        boolean relacaoCorreta = picos.entrySet().stream()
                .allMatch(e -> e.getValue() > e.getKey());
        if (relacaoCorreta) {
            resultado.append("✓ Teste 4.7: Próximo pico sempre posterior ao atual\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 4.7: Relação temporal incorreta\n");
        }
        
        // Teste 8: Não há ciclos (A -> B, B -> A)
        boolean semCiclos = true;
        for (Map.Entry<Long, Long> entry : picos.entrySet()) {
            if (picos.containsKey(entry.getValue()) && 
                picos.get(entry.getValue()).equals(entry.getKey())) {
                semCiclos = false;
                break;
            }
        }
        if (semCiclos) {
            resultado.append("✓ Teste 4.8: Não há ciclos na estrutura\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 4.8: Detectados ciclos\n");
        }
        
        // Teste 9: Verifica exemplo conhecido de pico
        boolean temPicoConhecido = picos.keySet().stream()
                .anyMatch(t -> t >= 1700002130L && t <= 1700002132L);
        if (temPicoConhecido) {
            resultado.append("✓ Teste 4.9: Detectou pico conhecido próximo a 1700002131\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 4.9: Não detectou pico conhecido\n");
        }
        
        // Teste 10: Executou com sucesso
        resultado.append("✓ Teste 4.10: Método executou sem exceções\n");
        testesPassados++;
        
        double pontos = calcularPontos(testesPassados, 10);
        resultado.append(String.format("Pontuação Desafio 4: %.2f/14.0 (%d/10 testes)\n\n", pontos, testesPassados));
        return pontos;
    }
    
    /**
     * Desafio 5: Rastrear Contaminação (10 testes)
     */
    private double testarDesafio5(Object instancia, Class<?> classe, String caminhoLogs, StringBuilder resultado) throws Exception {
        resultado.append("=== DESAFIO 5: Rastrear Contaminação (10 testes) ===\n");
        
        Method metodo = classe.getMethod("rastrearContaminacao", String.class, String.class, String.class);
        int testesPassados = 0;
        
        // Teste 1: Caminho de Alice para Bob
        List<String> caminhoAliceBob = (List<String>) metodo.invoke(instancia, caminhoLogs, "alice", "bob");
        if (caminhoAliceBob != null) {
            resultado.append("✓ Teste 5.1: Retornou resultado não-nulo (Alice -> Bob)\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 5.1: Retornou null\n");
            return calcularPontos(testesPassados, 10);
        }
        
        // Teste 2: Tipo correto (List)
        if (caminhoAliceBob instanceof List) {
            resultado.append("✓ Teste 5.2: Retornou uma List\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 5.2: Não retornou uma List\n");
        }
        
        // Teste 3: Caminho não vazio quando existe conexão
        if (!caminhoAliceBob.isEmpty()) {
            resultado.append("✓ Teste 5.3: Encontrou caminho Alice -> Bob (" + 
                           caminhoAliceBob.size() + " recursos)\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 5.3: Não encontrou caminho (pode ser correto se não houver)\n");
            testesPassados++; // Considera correto se vazio mas sem erro
        }
        
        // Teste 4: Primeiro elemento é Alice, último é Bob (se não vazio)
        if (!caminhoAliceBob.isEmpty()) {
            boolean inicioFimCorretos = 
                caminhoAliceBob.get(0).toLowerCase().contains("alice") &&
                caminhoAliceBob.get(caminhoAliceBob.size()-1).toLowerCase().contains("bob");
            if (inicioFimCorretos) {
                resultado.append("✓ Teste 5.4: Caminho inicia em Alice e termina em Bob\n");
                testesPassados++;
            } else {
                resultado.append("✗ Teste 5.4: Caminho não conecta corretamente Alice e Bob\n");
            }
        } else {
            resultado.append("✓ Teste 5.4: N/A - caminho vazio\n");
            testesPassados++;
        }
        
        // Teste 5: Caminho de Eve para Mallory
        List<String> caminhoEveMallory = (List<String>) metodo.invoke(instancia, caminhoLogs, "eve", "mallory");
        if (caminhoEveMallory != null) {
            resultado.append("✓ Teste 5.5: Funciona para Eve -> Mallory\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 5.5: Null para Eve -> Mallory\n");
        }
        
        // Teste 6: Caminho para si mesmo
        List<String> caminhoAliceAlice = (List<String>) metodo.invoke(instancia, caminhoLogs, "alice", "alice");
        if (caminhoAliceAlice != null && (caminhoAliceAlice.isEmpty() || caminhoAliceAlice.size() == 1)) {
            resultado.append("✓ Teste 5.6: Comportamento correto para origem = destino\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 5.6: Comportamento incorreto para origem = destino\n");
        }
        
        // Teste 7: Usuário inexistente como origem
        List<String> caminhoInexistente = (List<String>) metodo.invoke(instancia, caminhoLogs, "usuario_fake", "bob");
        if (caminhoInexistente != null && caminhoInexistente.isEmpty()) {
            resultado.append("✓ Teste 5.7: Retorna vazio para usuário inexistente\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 5.7: Comportamento incorreto para usuário inexistente\n");
        }
        
        // Teste 8: Case-insensitive
        List<String> caminhoUpper = (List<String>) metodo.invoke(instancia, caminhoLogs, "ALICE", "BOB");
        if (caminhoUpper != null && caminhoUpper.size() == caminhoAliceBob.size()) {
            resultado.append("✓ Teste 5.8: Busca é case-insensitive\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 5.8: Busca não é case-insensitive\n");
        }
        
        // Teste 9: Sem ciclos no caminho (elementos únicos ou caminho vazio)
        if (caminhoAliceBob.isEmpty() || 
            caminhoAliceBob.size() == new HashSet<>(caminhoAliceBob).size()) {
            resultado.append("✓ Teste 5.9: Caminho não contém ciclos\n");
            testesPassados++;
        } else {
            resultado.append("✗ Teste 5.9: Caminho contém elementos repetidos\n");
        }
        
        // Teste 10: Método robusto - não lança exceção
        try {
            metodo.invoke(instancia, caminhoLogs, "charlie", "dave");
            resultado.append("✓ Teste 5.10: Método robusto para diferentes entradas\n");
            testesPassados++;
        } catch (Exception e) {
            resultado.append("✗ Teste 5.10: Lançou exceção para entradas válidas\n");
        }
        
        double pontos = calcularPontos(testesPassados, 10);
        resultado.append(String.format("Pontuação Desafio 5: %.2f/14.0 (%d/10 testes)\n\n", pontos, testesPassados));
        return pontos;
    }
    
    /**
     * Calcula a pontuação usando a fórmula: Pontos = 14 × (Nc / Tt)
     */
    private double calcularPontos(int testesPassados, int totalTestes) {
        return 14.0 * ((double) testesPassados / totalTestes);
    }
    
    /**
     * Localiza o arquivo de logs
     */
    private String localizarArquivoLogs() {
        // Primeiro tenta no diretório pai do spring-app
        File arquivoParent = new File("../" + ARQUIVO_LOGS);
        if (arquivoParent.exists()) {
            return arquivoParent.getAbsolutePath();
        }
        
        // Tenta no diretório atual
        File arquivoAtual = new File(ARQUIVO_LOGS);
        if (arquivoAtual.exists()) {
            return arquivoAtual.getAbsolutePath();
        }
        
        // Tenta no workspace root
        File arquivoRoot = new File("../../" + ARQUIVO_LOGS);
        if (arquivoRoot.exists()) {
            return arquivoRoot.getAbsolutePath();
        }
        
        return null;
    }
    
    /**
     * Obtém as matrículas dos alunos
     */
    private String getMatriculas(Trabalho trabalho) {
        List<String> matriculas = new ArrayList<>();
        if (trabalho.getMatriculaAluno1() != null) matriculas.add(trabalho.getMatriculaAluno1());
        if (trabalho.getMatriculaAluno2() != null) matriculas.add(trabalho.getMatriculaAluno2());
        if (trabalho.getMatriculaAluno3() != null) matriculas.add(trabalho.getMatriculaAluno3());
        return String.join(", ", matriculas);
    }
}
