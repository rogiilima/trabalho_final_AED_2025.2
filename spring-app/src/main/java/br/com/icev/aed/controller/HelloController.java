package br.com.icev.aed.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // Removido mapeamento de "/" para não conflitar com index.html
    // O Spring Boot agora servirá o arquivo static/index.html automaticamente

    @GetMapping("/api/info")
    public String info() {
        return "Spring Boot Application - Sistema de Avaliação AED 2025.2";
    }
    
    @GetMapping("/api/health")
    public String health() {
        return "{\"status\":\"UP\",\"app\":\"aed-avaliacao\"}";
    }
}
