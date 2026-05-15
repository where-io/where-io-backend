# Feature: Logging Estruturado SLF4J + Logback com MDC

**Data:** 2026-04-05
**Status:** Planejado

---

## Resumo

Adicionar logging estruturado em JSON ao projeto where-io usando SLF4J + Logback com Mapped Diagnostic Context (MDC). Cada requisição HTTP receberá um `requestId` único (UUID) que será propagado por todas as camadas — controller, service, persistence adapter e external adapter — permitindo rastreabilidade ponta a ponta de uma requisição nos logs.

O formato JSON é produzido pelo `logstash-logback-encoder`, o que torna os logs diretamente consumíveis por ferramentas como Elasticsearch/Kibana, Datadog ou qualquer agregador que aceite JSON lines.

---

## Impacto Arquitetural

| Camada | Impacto |
|---|---|
| `config/` | Novo `MdcLoggingFilter` (Servlet Filter) registrado como Spring Bean |
| `adapters/in/web/` | `LocalController` e `VisitaController` recebem logs de entrada e saída |
| `application/service/` | Todos os 9 use case impls recebem logs de início, fim e erros de negócio |
| `adapters/out/persistence/` | `LocalRepositoryAdapter` e `VisitaRepositoryAdapter` logam operações MongoDB |
| `adapters/out/external/` | `GoogleGeocodingAdapter` loga chamadas à API externa com request/response summary |
| `exceptions/` | `GlobalExceptionHandler` loga a exceção com contexto MDC completo |
| `src/main/resources/` | Novo arquivo `logback-spring.xml` com appender JSON |
| `pom.xml` | Nova dependência `logstash-logback-encoder` |

Nenhuma interface de porta ou modelo de domínio é criado ou alterado. A feature é puramente transversal (cross-cutting concern).

---

## Novos Arquivos a Criar

### 1. Filtro MDC
**Caminho:** `src/main/java/analu/whereio/config/MdcLoggingFilter.java`
**Pacote:** `analu.whereio.config`
**Responsabilidade:** Interceptar toda requisição HTTP antes dos controllers, gerar um UUID como `requestId`, popular o MDC com `requestId`, `httpMethod`, `requestUri` e `clientIp`, e limpar o MDC após a resposta para evitar vazamento entre threads (thread pool reuse).

### 2. Configuração Logback
**Caminho:** `src/main/resources/logback-spring.xml`
**Responsabilidade:** Definir dois appenders — um JSON estruturado via `logstash-logback-encoder` (usado em produção via profile `prod`) e um legível para console (padrão de desenvolvimento). Configurar níveis de log por pacote.

---

## Arquivos Existentes a Modificar

### `pom.xml`
Adicionar a dependência `logstash-logback-encoder` dentro de `<dependencies>`.

### `src/main/java/analu/whereio/adapters/in/web/LocalController.java`
- Adicionar campo `private static final Logger log = LoggerFactory.getLogger(LocalController.class);`
- Em cada método: logar entrada (parâmetros relevantes) e saída (ID ou tamanho de lista retornado).

### `src/main/java/analu/whereio/adapters/in/web/VisitaController.java`
- Mesma abordagem de `LocalController`.

### `src/main/java/analu/whereio/application/service/local/CadastrarLocalUsecaseImpl.java`
- Logar início do use case com `local.getNome()`.
- Logar quando coordenadas são geradas pela API externa.
- Logar sucesso com o ID do Local persistido.

### `src/main/java/analu/whereio/application/service/local/AtualizarLocalUsecaseImpl.java`
- Logar início com `id`.
- Logar quando `BusinessException` é lançada (local não encontrado).
- Logar conclusão.

### `src/main/java/analu/whereio/application/service/local/RemoverLocalUsecaseImpl.java`
- Logar início com `id` e conclusão.

### `src/main/java/analu/whereio/application/service/local/BuscarLocalUsecaseImpl.java`
- Logar início com `inputText` e conclusão.

### `src/main/java/analu/whereio/application/service/local/BuscarTodosLocalUsecaseImpl.java`
- Logar início e quantidade de registros retornados.

### `src/main/java/analu/whereio/application/service/visita/CadastrarVisitaUsecaseImpl.java`
- Logar início com `visita.getIdLocal()`.
- Logar validação de existência do Local.
- Logar ID da visita criada.

### `src/main/java/analu/whereio/application/service/visita/AtualizarVisitaUsecaseImpl.java`
- Logar início com `idVisita` e conclusão.

### `src/main/java/analu/whereio/application/service/visita/RemoverVisitaUsecaseImpl.java`
- Logar início com `id` e conclusão.

### `src/main/java/analu/whereio/application/service/visita/BuscarVisitaPorIdLocalUsecaseImpl.java`
- Logar início com `idLocal` e quantidade de visitas retornadas.

### `src/main/java/analu/whereio/adapters/out/persistence/impl/LocalRepositoryAdapter.java`
- Logar cada operação MongoDB: `save`, `findByNome`, `findAll`, `findById`, `deleteById` com o ID ou nome relevante.

### `src/main/java/analu/whereio/adapters/out/persistence/impl/VisitaRepositoryAdapter.java`
- Mesma abordagem de `LocalRepositoryAdapter`.

### `src/main/java/analu/whereio/adapters/out/external/geocoding/impl/GoogleGeocodingAdapter.java`
- Logar início da chamada com o endereço (mascarado se necessário).
- Logar conclusão com `formatted_address` retornado.
- Logar erros da chamada HTTP com nível `ERROR`.

### `src/main/java/analu/whereio/exceptions/GlobalExceptionHandler.java`
- Logar `BusinessException` com nível `WARN` incluindo `status`, `message` e o MDC completo (que já estará populado pelo filtro).
- Logar exceções genéricas inesperadas com nível `ERROR` e stack trace.

---

## Modelos de Domínio

Nenhuma alteração nos modelos de domínio (`application/model/`). MDC é infraestrutura transversal — não pertence ao domínio.

---

## Ports

Nenhuma nova interface de porta é necessária. Logging é uma preocupação de infraestrutura que vive nas implementações, não nos contratos.

---

## Use Cases (`application/service/`)

Sem novos use cases. Apenas adição de instruções de log nas implementações existentes.

---

## DTOs e Converters (`adapters/in/web/`)

Sem novos DTOs. Sem alterações nos Converters.

---

## Entidades e Mappers (`adapters/out/persistence/`)

Sem alterações.

---

## Configuração do Filtro MDC

**Arquivo:** `src/main/java/analu/whereio/config/MdcLoggingFilter.java`

```java
package analu.whereio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID  = "requestId";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String REQUEST_URI = "requestUri";
    private static final String CLIENT_IP   = "clientIp";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            MDC.put(REQUEST_ID,  UUID.randomUUID().toString());
            MDC.put(HTTP_METHOD, request.getMethod());
            MDC.put(REQUEST_URI, request.getRequestURI());
            MDC.put(CLIENT_IP,   resolveClientIp(request));

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();  // OBRIGATORIO: evitar vazamento em thread pool
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

**Chaves MDC populadas pelo filtro:**

| Chave MDC | Valor |
|---|---|
| `requestId` | UUID v4 gerado por requisição |
| `httpMethod` | `GET`, `POST`, `PUT`, `DELETE` |
| `requestUri` | `/api/local`, `/api/visita/abc123` |
| `clientIp` | IP real, respeitando `X-Forwarded-For` |

**Chaves MDC adicionadas pontualmente nos services:**

| Chave MDC | Onde adicionar | Quando limpar |
|---|---|---|
| `operation` | Início de cada service method | Ao final do mesmo método |
| `entityId` | Quando um ID de entidade fica disponível | Ao final do mesmo método |

---

## Configuração Logback

**Arquivo:** `src/main/resources/logback-spring.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Appender JSON estruturado (logstash-logback-encoder) -->
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <!-- Inclui todos os campos MDC automaticamente -->
            <includeCallerData>false</includeCallerData>
            <!-- Campos customizados fixos -->
            <customFields>{"app":"where-io","environment":"local"}</customFields>
        </encoder>
    </appender>

    <!-- Appender legível para desenvolvimento -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} [reqId=%X{requestId}] - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Profile prod: JSON estruturado -->
    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
        </root>
    </springProfile>

    <!-- Profile default (dev/local): legível -->
    <springProfile name="!prod">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>

        <!-- Reduzir verbosidade de libs externas -->
        <logger name="org.springframework" level="WARN"/>
        <logger name="org.mongodb.driver"  level="WARN"/>

        <!-- Nível DEBUG nos pacotes do projeto -->
        <logger name="analu.whereio" level="DEBUG"/>
    </springProfile>

</configuration>
```

---

## Dependências e Configurações

### `pom.xml` — adicionar dentro de `<dependencies>`

```xml
<!-- Logback JSON encoder (logstash-logback-encoder) -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>8.0</version>
</dependency>
```

**Verificação de compatibilidade:** Spring Boot 4.x usa Logback 1.5.x. O `logstash-logback-encoder` 8.x é compatível com Logback 1.5.x. Versão 7.x usa Logback 1.4.x — não usar com Spring Boot 4.

**Observação:** SLF4J e Logback já estão presentes via `spring-boot-starter-webmvc` (transitivo através do `spring-boot-starter-logging`). Não é necessário declarar essas dependências explicitamente.

---

## Padrão de Logging por Camada

### Controllers (`adapters/in/web/`)

```java
// Padrão a seguir em LocalController e VisitaController:
private static final Logger log = LoggerFactory.getLogger(LocalController.class);

@PostMapping
ResponseEntity<String> cadastrarLocal(@Valid @RequestBody LocalDtoRequest localDtoRequest) {
    log.info("Requisicao para cadastrar local recebida. nome={}", localDtoRequest.getNome());

    LocalDtoResponse response = mapper.toResponse(
        cadastrarLocalUsecase.execute(mapper.toDomain(localDtoRequest))
    );

    log.info("Local cadastrado com sucesso. id={}", response.getId());
    return ResponseEntity.status(HttpStatus.OK).body(response.getId());
}
```

Nível `INFO` para entrada e saída. Nunca logar o objeto de request completo (pode conter dados sensíveis).

### Services (`application/service/`)

```java
// Padrão a seguir em todos os UsecaseImpl:
private static final Logger log = LoggerFactory.getLogger(CadastrarLocalUsecaseImpl.class);

@Override
public Local execute(Local local) {
    MDC.put("operation", "cadastrarLocal");
    log.info("Iniciando cadastro de local. nome={}", local.getNome());

    try {
        if (!isNull(localRepositoryPort.buscarPorNomeLocal(local.getNome()))) {
            log.warn("Tentativa de cadastro de local duplicado. nome={}", local.getNome());
            throw new BusinessException("Local já foi cadastrado", HttpStatus.UNPROCESSABLE_CONTENT);
        }

        if (isNull(local.getCoordenadas().getLongitude()) || isNull(local.getCoordenadas().getLatitude())) {
            log.debug("Coordenadas ausentes, buscando via API externa. endereco={}", local.getEndereco());
            // ... chamada à API
            log.debug("Coordenadas obtidas com sucesso. lat={} lng={}",
                coordenadas.getLatitude(), coordenadas.getLongitude());
        }

        Local salvo = localRepositoryPort.cadastrarLocal(local);
        MDC.put("entityId", salvo.getId());
        log.info("Local cadastrado com sucesso. id={}", salvo.getId());
        return salvo;

    } finally {
        MDC.remove("operation");
        MDC.remove("entityId");
    }
}
```

Nível `INFO` para início/fim. `WARN` para regras de negócio violadas. `DEBUG` para detalhes intermediários.

### Persistence Adapters (`adapters/out/persistence/`)

```java
// Padrão a seguir em LocalRepositoryAdapter e VisitaRepositoryAdapter:
private static final Logger log = LoggerFactory.getLogger(LocalRepositoryAdapter.class);

@Override
public Local cadastrarLocal(Local local) {
    log.debug("Persistindo local no MongoDB. nome={}", local.getNome());
    Local salvo = mapper.toDomain(repository.save(mapper.toEntity(local)));
    log.debug("Local persistido com sucesso. id={}", salvo.getId());
    return salvo;
}

@Override
public void removerLocalPorId(String id) {
    log.debug("Removendo local do MongoDB. id={}", id);
    repository.deleteById(id);
    log.debug("Local removido do MongoDB. id={}", id);
}
```

Nível `DEBUG` para todos os acessos ao banco (verboso, mas visível somente quando necessário).

### External Adapters (`adapters/out/external/`)

```java
// Padrão a seguir em GoogleGeocodingAdapter:
private static final Logger log = LoggerFactory.getLogger(GoogleGeocodingAdapter.class);

public ApiResponse buscarLocalizacao(String endereco) throws IOException, InterruptedException {
    log.info("Chamando Google Geocoding API. endereco={}", endereco);
    try {
        ApiResponse response = webClient
            .get()
            .uri(/* ... */)
            .retrieve()
            .bodyToMono(ApiResponse.class)
            .block();

        log.info("Resposta recebida do Google Geocoding API. resultados={}",
            response != null && response.getResults() != null
                ? response.getResults().size() : 0);
        return response;

    } catch (Exception e) {
        log.error("Falha na chamada ao Google Geocoding API. endereco={} erro={}",
            endereco, e.getMessage(), e);
        throw e;
    }
}
```

Nível `INFO` para início e fim de chamadas externas. `ERROR` com stack trace para falhas.

### GlobalExceptionHandler (`exceptions/`)

```java
// Modificacao em GlobalExceptionHandler:
private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    log.warn("Excecao de negocio. status={} message={}", ex.getStatus().value(), ex.getMessage());

    ErrorResponse error = new ErrorResponse(ex.getStatus().value(), ex.getMessage());
    return new ResponseEntity<>(error, ex.getStatus());
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Excecao inesperada nao tratada. message={}", ex.getMessage(), ex);

    ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Ocorreu um erro interno no servidor");
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
}
```

---

## Passo a Passo de Implementação

A ordem abaixo garante que cada passo compile sem depender de código ainda não escrito.

**Passo 1 — Dependência Maven**
Adicionar `logstash-logback-encoder 8.0` no `pom.xml`. Executar `./mvnw dependency:resolve` para verificar que baixa sem conflito.

**Passo 2 — Arquivo logback-spring.xml**
Criar `src/main/resources/logback-spring.xml` com os dois appenders (JSON e CONSOLE) e as configurações de profile. Não depende de nenhum código Java — pode ser feito imediatamente após o passo 1.

**Passo 3 — MdcLoggingFilter**
Criar `src/main/java/analu/whereio/config/MdcLoggingFilter.java`. Depende apenas de `jakarta.servlet` (já disponível) e `org.slf4j.MDC` (já disponível via Spring Boot). Subir a aplicação e verificar que o filtro é registrado no log de startup.

**Passo 4 — GlobalExceptionHandler**
Adicionar o `Logger` e os dois métodos `@ExceptionHandler` (o existente `handleBusinessException` modificado + novo `handleGenericException`). Este passo é seguro — não há nova dependência de código.

**Passo 5 — Persistence Adapters**
Adicionar Logger e chamadas `log.debug()` em `LocalRepositoryAdapter` e `VisitaRepositoryAdapter`. Estes adaptadores não têm dependências circulares.

**Passo 6 — External Adapter**
Adicionar Logger e chamadas `log.info()` / `log.error()` em `GoogleGeocodingAdapter`. Nível `INFO` para chamadas externas (operações custosas merecem visibilidade).

**Passo 7 — Services de Local**
Adicionar Logger e MDC contextual nos 5 services de Local na seguinte sub-ordem:
- `RemoverLocalUsecaseImpl` (mais simples)
- `BuscarTodosLocalUsecaseImpl`
- `BuscarLocalUsecaseImpl`
- `AtualizarLocalUsecaseImpl`
- `CadastrarLocalUsecaseImpl` (mais complexo, coordenadas)

**Passo 8 — Services de Visita**
Mesma abordagem para os 4 services de Visita:
- `RemoverVisitaUsecaseImpl`
- `BuscarVisitaPorIdLocalUsecaseImpl`
- `AtualizarVisitaUsecaseImpl`
- `CadastrarVisitaUsecaseImpl`

**Passo 9 — Controllers**
Adicionar Logger em `LocalController` e `VisitaController`. Controllers vêm por último pois dependem dos services (que já estarão instrumentados).

**Passo 10 — Verificação de integração**
Subir a aplicação localmente e fazer uma requisição `POST /api/local`. Verificar no console que a linha de log contém `requestId`, `httpMethod`, `requestUri` e `clientIp`. Verificar que o MDC é limpo após a requisição (sem `requestId` em logs do próximo ciclo de startup).

---

## Tratamento de Erros

| Situação | Nível | Classe que loga |
|---|---|---|
| `BusinessException` capturada | `WARN` | `GlobalExceptionHandler` |
| Exceção genérica inesperada | `ERROR` + stack trace | `GlobalExceptionHandler` |
| Regra de negócio violada antes de lançar | `WARN` | Use case impl |
| Falha em chamada à API externa | `ERROR` + stack trace | `GoogleGeocodingAdapter` |
| Operação MongoDB | `DEBUG` | Persistence adapters |
| Entrada/saída de controller | `INFO` | Controllers |
| Início/fim de use case | `INFO` | Service impls |

---

## Testes Sugeridos

### `MdcLoggingFilterTest`
**Pacote:** `analu.whereio.config`
- Verificar que MDC é populado com `requestId` (UUID válido) durante a requisição
- Verificar que MDC é limpo após a requisição (`MDC.get("requestId")` retorna null)
- Verificar que `X-Forwarded-For` tem precedência sobre `remoteAddr` para `clientIp`

**Abordagem:** `MockMvc` com `MockHttpServletRequest`, sem necessidade de contexto Spring completo.

### Modificações nos testes existentes
Os testes de controller existentes (`LocalControllerTest`, `VisitaControllerTest`) usam `@WebMvcTest` e portanto automaticamente registram Servlet Filters. Adicionar a dependência `MdcLoggingFilter` no contexto de teste via `@Import(MdcLoggingFilter.class)` ou `@WebMvcTest` que já inclui `@Component` beans de `config/`.

Os testes de service e de adapter não precisam de alteração — os logs adicionados não alteram comportamento.

---

## Riscos e Pontos de Atenção

**1. Vazamento de MDC em pool de threads**
O `MDC.clear()` no bloco `finally` do filtro resolve o problema para a thread principal. Porém, se forem usadas `@Async`, `CompletableFuture` ou Reactor (WebFlux), o MDC não é propagado automaticamente para threads secundárias. No estado atual do projeto não há uso de `@Async`, mas a equipe deve estar ciente para features futuras.

**2. Logback já configurado pelo Spring Boot auto-configuration**
O Spring Boot configura o Logback automaticamente. O arquivo `logback-spring.xml` (com sufixo `-spring`) permite usar `<springProfile>` e tem precedência sobre `logback.xml`. Usar `logback-spring.xml` e não `logback.xml`.

**3. Versão do logstash-logback-encoder**
Spring Boot 4.x depende de Logback 1.5.x. Usar `logstash-logback-encoder` versão 8.x. Versões anteriores (7.x) são incompatíveis com Logback 1.5.x e causam `ClassNotFoundException` em runtime.

**4. Dados sensíveis nos logs**
Nunca logar o objeto de request completo, tokens de sessão (campo `sessionToken` do `LocalBuscarDtoRequest`) ou chaves de API. O endereço do cliente pode ser considerado dado pessoal dependendo da legislação — avaliar mascaramento parcial se necessário (ex: `192.168.*.*`).

**5. Performance em produção**
O nível `DEBUG` nos pacotes do projeto deve ser usado apenas em desenvolvimento. Em produção (`profile=prod`), o nível `INFO` já remove todas as chamadas `log.debug()` sem custo de processamento (SLF4J usa guard automático).

**6. Conflito com configuração de CORS**
O `MdcLoggingFilter` com `@Order(1)` executará antes dos filtros de CORS. Isso é intencional — queremos o `requestId` disponível desde o início. Verificar se a ordem não conflita com eventuais filtros de autenticação futuros (estes devem ter `@Order` maior que 1).

---

## Critérios de Aceite

- [ ] Ao fazer `POST /api/local`, o console exibe pelo menos 3 linhas de log com o mesmo valor de `requestId`
- [ ] Cada linha de log contém os campos `requestId`, `httpMethod`, `requestUri` e `clientIp`
- [ ] Ao fazer duas requisições em sequência, os `requestId` são diferentes (UUID distintos)
- [ ] Com profile `prod` ativo, os logs são emitidos em formato JSON válido (uma linha por evento)
- [ ] Com profile padrão (dev), os logs são emitidos em formato legível com `[reqId=...]` visível
- [ ] `BusinessException` aparece como `WARN` no log com `status` e `message`
- [ ] Exceções inesperadas aparecem como `ERROR` com stack trace
- [ ] Após o fim de uma requisição, o MDC não vaza para a próxima (verificável observando logs de startup sem `requestId`)
- [ ] O token de sessão do endpoint `GET /api/local/buscar-local` não aparece em nenhum log
- [ ] Todos os testes existentes continuam passando sem modificação de assertions (`./mvnw test` verde)
