package com.example.demo.service;

import com.example.demo.entity.Trabalho;
import com.example.demo.repository.TrabalhoRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrabalhoLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(TrabalhoLoaderService.class);

    @Autowired
    private TrabalhoRepository trabalhoRepository;

    @Value("${trabalhos.pasta:projetos_alunos}")
    private String pastaProjetos;

    @PostConstruct
    public void loadTrabalhosFromDirectory() {
        carregarTrabalhos();
    }

    public String carregarTrabalhos() {
        logger.info("Iniciando carregamento de trabalhos da pasta: {}", pastaProjetos);

        File directory = new File(pastaProjetos);
        
        // Se o caminho for relativo, tenta a partir do diretório raiz do projeto
        if (!directory.isAbsolute()) {
            String projectRoot = System.getProperty("user.dir");
            // Se estamos em spring-app, sobe um nível
            if (projectRoot.endsWith("spring-app")) {
                projectRoot = new File(projectRoot).getParent();
            }
            directory = new File(projectRoot, pastaProjetos);
        }

        if (!directory.exists() || !directory.isDirectory()) {
            String mensagem = "Pasta de projetos não encontrada: " + directory.getAbsolutePath();
            logger.warn(mensagem);
            return mensagem;
        }

        File[] jarFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        
        if (jarFiles == null || jarFiles.length == 0) {
            String mensagem = "Nenhum arquivo JAR encontrado na pasta";
            logger.info(mensagem);
            return mensagem;
        }

        logger.info("Encontrados {} arquivos JAR", jarFiles.length);

        int novosTrabalhos = 0;
        int trabalhosPulados = 0;

        for (File jarFile : jarFiles) {
            try {
                String caminhoCompleto = jarFile.getAbsolutePath();
                
                // Verifica se já existe um trabalho com este caminho
                if (trabalhoRepository.findByCaminhoParaJar(caminhoCompleto).isPresent()) {
                    logger.debug("Trabalho já existe para o JAR: {}", jarFile.getName());
                    trabalhosPulados++;
                    continue;
                }

                // Extrai as matrículas do nome do arquivo
                String nomeArquivo = jarFile.getName().replace(".jar", "");
                String[] matriculas = nomeArquivo.split("_");

                if (matriculas.length == 0 || matriculas.length > 3) {
                    logger.warn("Nome de arquivo inválido (deve ter 1 a 3 matrículas): {}", jarFile.getName());
                    continue;
                }

                // Cria o objeto Trabalho
                Trabalho trabalho = new Trabalho();
                trabalho.setTitulo("Trabalho Forense AED - " + nomeArquivo);
                trabalho.setCaminhoParaJar(caminhoCompleto);
                trabalho.setMatriculaAluno1(matriculas[0]);
                
                if (matriculas.length >= 2) {
                    trabalho.setMatriculaAluno2(matriculas[1]);
                }
                
                if (matriculas.length == 3) {
                    trabalho.setMatriculaAluno3(matriculas[2]);
                }

                trabalho.setDescricao("Trabalho importado automaticamente da pasta projetos_alunos");
                trabalho.setDataEntrega(LocalDate.now().format(DateTimeFormatter.ISO_DATE));

                // Salva no banco
                trabalhoRepository.save(trabalho);
                novosTrabalhos++;

                logger.info("Trabalho cadastrado: {} (Alunos: {})", 
                    trabalho.getTitulo(), 
                    String.join(", ", getMatriculasNaoNulas(trabalho)));

            } catch (Exception e) {
                logger.error("Erro ao processar arquivo {}: {}", jarFile.getName(), e.getMessage());
            }
        }

        String mensagem = String.format("Carregamento concluído: %d novos trabalhos cadastrados, %d já existiam", 
            novosTrabalhos, trabalhosPulados);
        logger.info(mensagem);
        return mensagem;
    }

    private List<String> getMatriculasNaoNulas(Trabalho trabalho) {
        List<String> matriculas = new ArrayList<>();
        if (trabalho.getMatriculaAluno1() != null) matriculas.add(trabalho.getMatriculaAluno1());
        if (trabalho.getMatriculaAluno2() != null) matriculas.add(trabalho.getMatriculaAluno2());
        if (trabalho.getMatriculaAluno3() != null) matriculas.add(trabalho.getMatriculaAluno3());
        return matriculas;
    }
}
