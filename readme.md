# 🚀 Spring Boot 3 Clean Architecture & Event-Driven Starter Kit

![Java 21](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=flat-square&logo=springboot)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-black?style=flat-square&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![ArchUnit](https://img.shields.io/badge/ArchUnit-1.3-yellow?style=flat-square)
![OpenAPI 3](https://img.shields.io/badge/OpenAPI-3.0%20%2F%20Swagger-green?style=flat-square&logo=swagger)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker)

Template base pronto para produção voltado para criação rápida de novos microsserviços e APIs corporativas em Java 21 e Spring Boot 3. 

Projetado seguindo **Clean Architecture**, **Domain-Driven Design (DDD)**, **Event-Driven Architecture (EDA)** com **Apache Kafka**, autenticação stateless com **JWT**, guardrails arquiteturais automatizados via **ArchUnit**, testes de integração com **EmbeddedKafka** e **Testcontainers**, e documentação interativa com **OpenAPI 3 (Swagger)**.

---

## 🏛️ Princípios e Arquitetura

### 1. Separação Estrita de Camadas (Clean Architecture & DDD)

```
                       ┌─────────────────────────────────────┐
                       │          Infra (Adapters)           │
                       │  Controllers, JPA, Kafka, Security  │
                       └──────────────────┬──────────────────┘
                                          │ depende de
                                          ▼
                       ┌─────────────────────────────────────┐
                       │        Application (UseCases)       │
                       │     Commands, Queries, Results      │
                       └──────────────────┬──────────────────┘
                                          │ depende de
                                          ▼
                       ┌─────────────────────────────────────┐
                       │           Domain (Core)             │
                       │ Entities, Value Objects, Aggregates │
                       │    Domain Events, Repositories      │
                       └─────────────────────────────────────┘
```

- **`domain`**: Núcleo isolado da aplicação. Contém entidades ricas, Agregados, Value Objects imutáveis e contratos de repositórios (interfaces). **Não possui dependência de frameworks** (zero anotações Spring, zero JPA, zero libs HTTP).
- **`application`**: Casos de uso de negócio (`CreateUserUseCase`, `ChangeUserPasswordUseCase`, `LoginUseCase`). Orquestra a execução, controla transações de aplicação e gerencia fluxos via DTOs dedicados (`Command`, `Query`, `Result`).
- **`infra`**: Camada de entrega e detalhes tecnológicos:
  - `controller`: Endpoints REST, mapeamento de requisições (`toCommand()`) e respostas (`from(result)`).
  - `persistence`: Entidades JPA, repositórios Spring Data e implementações dos contratos de repositório do domínio.
  - `security`: Filtro JWT, validação de tokens e hashing seguro com BCrypt.
  - `kafka`: Publicação de eventos de domínio (`KafkaDomainEventPublisher`) e listeners assíncronos (`UserEventsConsumer`).
  - `config`: Beans de configuração (OpenAPI, Spring Security, Seeds de inicialização).
  - `exception`: Tratamento global de exceções centralizado (`GlobalExceptionHandler`).

---

### 2. Fluxo Orientado a Eventos (Event-Driven com Kafka)

O projeto adota o padrão **Pull Model** para eventos de domínio em Aggregates:
1. **Mutação no Agregado:** Quando um estado de negócio relevante é alterado (ex: criação de usuário ou alteração de senha), a entidade de domínio registra internamente o evento correspondente (`user.registerEvent(new UserCreatedEvent(...))`).
2. **Drenagem pelo Caso de Uso:** O Use Case persiste o agregado e extrai a lista de eventos pendentes (`user.pullEvents()`).
3. **Publicação Desacoplada:** O caso de uso delega a publicação para a interface `DomainEventPublisher`.
4. **Envio ao Kafka:** A implementação de infraestrutura `KafkaDomainEventPublisher` identifica o tópico correto através do `KafkaTopicRegistry` e publica o evento serializado em JSON.
5. **Consumo Assíncrono:** Consumidores Kafka (`@KafkaListener`) processam os eventos assincronamente em seus respectivos tópicos.

---

### 3. Guardrails Automatizados de Arquitetura (ArchUnit)

O projeto inclui uma suíte automatizada de testes arquiteturais ([`CleanArchitectureTest`](file:///src/test/java/com/example/api_docker/architecture/CleanArchitectureTest.java)) que roda em **~0.5s** durante o `mvn test`:

| Regra | Objetivo |
|---|---|
| **Pureza do Domínio** | Impede qualquer import de `application`, `infra`, `org.springframework..` ou `jakarta.persistence..` no pacote `domain`. |
| **Isolamento da Aplicação** | Impede que a camada `application` importe pacotes de `infra` ou do Spring Web (`org.springframework.web..`). |
| **Injeção Segura** | Proíbe injeção direta em campos (`@Autowired` ou `@Value` em private fields), forçando injeção por construtor. |
| **Inversão de Dependência (DIP)** | Garante que qualquer classe em `domain` que termine com `Repository` seja obrigatoriamente uma `interface`. |
| **Padronização de Pacotes** | Garante que classes terminadas em `Controller` estejam em `infra.controller` e `UseCase` em `application..usecase`. |

---

## 📁 Estrutura de Diretórios

```
src/main/java/com/example/api_docker/
├── domain/
│   ├── shared/             # Classes base (AggregateRoot, DomainEvent, EntityId, ValueObject)
│   └── user/               # Agregado User, Email, Password, Eventos e UserRepository
├── application/
│   ├── auth/               # Commands, Results e UseCase de Autenticação / Login
│   └── user/               # Commands, Queries, Results e UseCases de Usuário
└── infra/
    ├── config/             # Configurações do Spring (OpenApiConfig, SecurityConfig, UserSeedConfig)
    ├── controller/         # Controllers REST e DTOs (Request / Response)
    ├── exception/          # GlobalExceptionHandler e ErrorResponse padronizado
    ├── kafka/              # Publisher, Topic Registry e Consumers Kafka
    ├── persistence/        # Entidades JPA e Repositórios Spring Data
    └── security/           # Filtro JWT, Token Generator e BCrypt
```

---

## ⚡ Como Executar o Projeto

### Pré-requisitos
- **Docker** e **Docker Compose**
- **Java 21** (necessário apenas para rodar localmente fora do container)

---

### Opção A: Desenvolvimento Local (API no Host / IDE + Infra no Docker)

Ideal para o dia a dia de desenvolvimento rápido com live reload e debugging no IntelliJ/VSCode:

1. **Crie a rede Docker compartilhada e suba a infraestrutura:**
   ```bash
   # Cria a rede compartilhada se ainda não existir
   docker network create api-network

   # Sobe o banco PostgreSQL e o Apache Kafka em modo KRaft (sem Zookeeper)
   docker-compose up -d
   ```

2. **Execute a aplicação Spring Boot:**
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Ou execute o método `main` da classe `ApiDockerApplication` diretamente pela sua IDE).*

---

### Opção B: Deploy e Containerização Completa (Production-Ready com `compose.deploy`)

Para rodar todo o ecossistema (Aplicação + Banco + Kafka) 100% conteinerizado de forma idêntica à produção:

O projeto utiliza uma estratégia de **Multi-stage Build** no [`Dockerfile`](file:///Dockerfile):
- **Estágio 1 (Build):** Imagem Maven com Java 21 compila o código e gera o `.jar`.
- **Estágio 2 (Runtime):** O `.jar` compilado é copiado para uma imagem enxuta baseada em `eclipse-temurin:21-jre`, garantindo performance, inicialização rápida e menor superfície de vulnerabilidades.

#### Passo a Passo para Subir o Ambiente Completo:

1. **Crie a rede compartilhada:**
   ```bash
   docker network create api-network
   ```

2. **Suba os serviços de infraestrutura (PostgreSQL e Kafka):**
   ```bash
   docker-compose up -d
   ```

3. **Compile e inicie o container da aplicação:**
   ```bash
   docker-compose -f docker-compose.deploy.yml up --build -d
   ```

#### Comandos Úteis do Container da Aplicação:

- **Acompanhar os logs da aplicação:**
  ```bash
  docker logs -f spring-app-course
  ```

- **Atualizar a aplicação após alterações no código:**
  ```bash
  # O parâmetro --build força uma nova compilação no container
  docker-compose -f docker-compose.deploy.yml up --build -d
  ```

- **Parar a aplicação e a infraestrutura:**
  ```bash
  # Para a aplicação
  docker-compose -f docker-compose.deploy.yml down

  # Para o banco e kafka
  docker-compose down
  ```

---

### 🔑 Credenciais Padrão (Seed Admin)
Ao inicializar a aplicação (seja localmente ou via container), um administrador é provisionado automaticamente:
- **Email:** `admin@course.com`
- **Senha padrão:** `MaluZoe` *(customizável via variável de ambiente `ADMIN_SEED_PASSWORD`)*
- **Health Check:** [http://localhost:8080/health](http://localhost:8080/health)

---

## 📖 Documentação Interativa & Testes (OpenAPI 3 / Swagger)

A API possui documentação interativa gerada automaticamente com suporte a autenticação via **Bearer Token (JWT)**:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Como testar endpoints protegidos no Swagger UI:
1. Acesse o Swagger UI no navegador.
2. Abra a seção **Autenticação** e execute o endpoint `POST /auth/login` com as credenciais do admin (`admin@course.com` / `MaluZoe`).
3. Copie o valor do campo `token` retornado no corpo da resposta.
4. No topo da página do Swagger, clique no botão verde **Authorize** (com ícone de cadeado).
5. Cole o token no campo de valor e confirme.
6. Todos os endpoints protegidos (`GET /user/me`, `POST /user/register`, `PATCH /user/password`) agora podem ser executados diretamente pelo navegador!

### 🚀 Importação no Bruno ou Postman:
Você pode importar todas as rotas e tipos diretamente no **Bruno** ou **Postman**:
1. No seu client HTTP, selecione a opção **Import**.
2. Escolha importar via URL OpenAPI / Swagger.
3. Insira a URL: `http://localhost:8080/v3/api-docs`.

---

## 🧪 Estratégia e Execução de Testes

O projeto conta com uma pirâmide completa de testes automatizados:
1. **Testes de Arquitetura:** Validação contínua com ArchUnit.
2. **Testes Unitários:** Testes de domínio, Value Objects e Use Cases isolados com Mockito.
3. **Testes de Controller:** Testes de camada web com `MockMvc` e validações de DTO.
4. **Testes de Integração:** Testes com H2 em memória e PostgreSQL real via Testcontainers.
5. **Testes End-to-End (E2E) com Kafka:** Fluxo completo via `@EmbeddedKafka` validando: chamada HTTP -> Controller -> Banco de Dados -> Disparo de Evento -> Consumo pelo Listener Kafka.

Para executar todos os testes da aplicação:
```bash
./mvnw test
```

---

## 🛠️ Como Usar este Repositório como Novo Projeto (Starter Kit)

Para iniciar um novo projeto a partir deste repositório:

1. **Clonar ou Criar Repositório a partir deste:**
   ```bash
   git clone <URL_DESTE_REPOSITORIO> meu-novo-microsservico
   cd meu-novo-microsservico
   rm -rf .git
   git init
   ```
2. **Atualizar Metadados do Projeto no `pom.xml`:**
   - Altere `<groupId>`, `<artifactId>`, `<name>` e `<description>`.
3. **Ajustar Nome da Aplicação:**
   - Em `src/main/resources/application.properties`, altere `spring.application.name`.
4. **Modelar seu Novo Domínio:**
   - Crie seu agregado dentro de `domain/<novo-dominio>/` estendendo `AggregateRoot`.
   - Defina Value Objects e eventos de domínio.
   - Crie os UseCases em `application/<novo-dominio>/usecase/`.
   - Adicione os controllers e DTOs em `infra/controller/<novo-dominio>/`.
   - Registre os novos tópicos Kafka em `KafkaTopicRegistry.java` e `EventType.java`.
5. **Validar:**
   - Rode `./mvnw test` e tenha certeza de que todas as regras do ArchUnit continuam respeitadas!

---

## 📄 Licença
Distribuído sob a licença MIT. Consulte `LICENSE` para obter mais informações.