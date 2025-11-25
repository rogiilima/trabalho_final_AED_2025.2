# Spring Boot com SQLite

Aplicação Spring Boot com banco de dados SQLite e API REST completa para gerenciamento de usuários.

## Requisitos

- Java 21
- Maven 3.6+

## Tecnologias

- Spring Boot 3.2.0
- Spring Data JPA
- SQLite Database
- Hibernate

## Como executar

```bash
cd spring-app
mvn clean package
java -jar target/demo-1.0.0.jar
```

A aplicação iniciará na porta 8080 e criará automaticamente o banco de dados `database.db` na raiz do projeto.

## Endpoints

### Hello World
- `GET /` - Retorna "Hello World!"
- `GET /api/info` - Retorna informações da aplicação

### API de Usuários (CRUD Completo)

#### Listar todos os usuários
```bash
GET /api/users
curl http://localhost:8080/api/users
```

#### Buscar usuário por ID
```bash
GET /api/users/{id}
curl http://localhost:8080/api/users/1
```

#### Buscar usuário por email
```bash
GET /api/users/email/{email}
curl http://localhost:8080/api/users/email/teste@email.com
```

#### Criar novo usuário
```bash
POST /api/users
Content-Type: application/json

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"João Silva","email":"joao@email.com","age":25}'
```

#### Atualizar usuário
```bash
PUT /api/users/{id}
Content-Type: application/json

curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"João Santos","email":"joao.santos@email.com","age":26}'
```

#### Deletar usuário
```bash
DELETE /api/users/{id}
curl -X DELETE http://localhost:8080/api/users/1
```

#### Deletar todos os usuários
```bash
DELETE /api/users
curl -X DELETE http://localhost:8080/api/users
```

## Estrutura do Projeto

```
spring-app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/demo/
│       │       ├── DemoApplication.java
│       │       ├── controller/
│       │       │   ├── HelloController.java
│       │       │   └── UserController.java
│       │       ├── entity/
│       │       │   └── User.java
│       │       └── repository/
│       │           └── UserRepository.java
│       └── resources/
│           └── application.properties
├── pom.xml
└── database.db (criado automaticamente)
```

## Modelo de Dados

### User
- `id` (Long) - Chave primária, auto incremento
- `name` (String) - Nome do usuário, obrigatório
- `email` (String) - Email único, obrigatório
- `age` (Integer) - Idade, opcional

## Exemplos de Uso

### 1. Criar usuários
```bash
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name":"Maria","email":"maria@email.com","age":30}'
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name":"Pedro","email":"pedro@email.com","age":28}'
```

### 2. Listar todos
```bash
curl http://localhost:8080/api/users
```

### 3. Buscar por ID
```bash
curl http://localhost:8080/api/users/1
```

### 4. Atualizar
```bash
curl -X PUT http://localhost:8080/api/users/1 -H "Content-Type: application/json" -d '{"name":"Maria Silva","email":"maria.silva@email.com","age":31}'
```

### 5. Deletar
```bash
curl -X DELETE http://localhost:8080/api/users/1
```
