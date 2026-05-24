# User & Friend System — Design Spec
**Data:** 2026-05-24  
**Branch alvo:** `feat/user-friend-system`

---

## Contexto

O sistema atual tem `nomeUsuario` como campo opcional/sparse em `UserEntity`. Convites de amizade já usam `nomeUsuarioDestino`, mas não há busca autocomplete, foto de perfil, nem restrições de formato no `nomeUsuario`. Este spec cobre as melhorias necessárias.

---

## 1. Modelo de Dados

### `UserEntity` / `UserAccount` — alterações

| Campo | Antes | Depois |
|-------|-------|--------|
| `nomeUsuario` | `@Indexed(unique=true, sparse=true)` | `@Indexed(unique=true)` — obrigatório |
| `fotoPerfil` | inexistente | `String` (key no `FileStoragePort`, nullable) |

**Validação de `nomeUsuario`:**
- Somente letras minúsculas, números e underscore: `[a-z0-9_]{3,20}`
- Imutável após criação (sem endpoint de alteração)
- Aplicada em `RegisterRequest` via `@NotBlank` + `@Pattern`

### `RegisterRequest` — alterações

```java
@NotBlank
@Pattern(regexp = "^[a-z0-9_]{3,20}$", message = "nomeUsuario: 3–20 chars, apenas a-z, 0-9, _")
private String nomeUsuario;  // deixa de ser opcional
```

### `AmigoPerfilResponse` — alterações

| Campo | Antes | Depois |
|-------|-------|--------|
| `email` | presente | **removido** (privacidade) |
| `fotoPerfilUrl` | inexistente | `String` (URL assinada, nullable) |

### Novo DTO: `UsuarioBuscaResponse`

```java
String nomeUsuario;
String nome;
String fotoPerfilUrl;  // nullable
```

### Novo DTO: `UsuarioPerfilResponse`

```java
String id;
String nome;
String nomeUsuario;
String email;
String fotoPerfilUrl;  // nullable
```

### Novo DTO request: `AtualizarNomeRequest`

```java
@NotBlank
@Size(min = 2, max = 80)
String nome;
```

---

## 2. Repositório

`AppUserMongoRepository` — novos métodos:

```java
Optional<UserEntity> findByNomeUsuario(String nomeUsuario);
boolean existsByNomeUsuario(String nomeUsuario);

// Busca autocomplete — prefixo case-insensitive, limite via Pageable
List<UserEntity> findByNomeUsuarioStartingWithIgnoreCase(String prefix, Pageable pageable);
```

**Índice MongoDB:** `nomeUsuario` já tem `@Indexed(unique=true)` — suporta range scan para prefixo.

---

## 3. Ports (Use Cases)

Novos ports em `application/ports/in/usuario/`:

| Interface | Método | Descrição |
|-----------|--------|-----------|
| `BuscarUsuariosPorPrefixoUsecase` | `execute(String prefix, String requesterId)` → `List<UserAccount>` | Autocomplete, exclui amigos confirmados do `requesterId`, limite 10 |
| `ObterPerfilUsuarioUsecase` | `execute(String userId)` → `UserAccount` | Dados do usuário autenticado |
| `AtualizarNomeUsuarioUsecase` | `execute(String userId, String novoNome)` → `UserAccount` | Atualiza `nome` |
| `AtualizarFotoPerfilUsecase` | `execute(MultipartFile file, String userId)` → `String` | Salva arquivo, atualiza `fotoPerfil` key, retorna URL assinada |
| `RemoverFotoPerfilUsecase` | `execute(String userId)` | Remove arquivo do storage e limpa campo `fotoPerfil` |

Port de saída existente `UserAccountRepositoryPort` — novos métodos:

```java
Optional<UserAccount> findByNomeUsuario(String nomeUsuario);   // substitui findByNome (bugfix)
boolean existsByNomeUsuario(String nomeUsuario);
List<UserAccount> buscarPorPrefixoNomeUsuario(String prefix, int limit);
```

> **Bug fix:** `findByNome` / `AppUserMongoRepository.findByNome` estão mal nomeados — Spring Data gera query no campo `nome` (display name), não `nomeUsuario`. Renomear para `findByNomeUsuario` em toda a cadeia.

---

## 4. Endpoints

Todos os endpoints abaixo requerem autenticação JWT.

### `UserController` — `/api/usuario`

| Método | Path | Body / Param | Response | Status |
|--------|------|-------------|----------|--------|
| `GET` | `/api/usuario/buscar` | `?q={prefix}` (min 1 char) | `List<UsuarioBuscaResponse>` (máx 10) | 200 |
| `GET` | `/api/usuario/perfil` | — | `UsuarioPerfilResponse` | 200 |
| `PATCH` | `/api/usuario/nome` | `AtualizarNomeRequest` (JSON) | `UsuarioPerfilResponse` | 200 |
| `PUT` | `/api/usuario/foto-perfil` | `multipart/form-data`, campo `file` | `FileUploadResponse` | 200 |
| `DELETE` | `/api/usuario/foto-perfil` | — | `204 No Content` | 204 |

### `AuthController` — mudança em endpoint existente

| Método | Path | Mudança |
|--------|------|---------|
| `POST` | `/api/auth/register` | `nomeUsuario` passa a ser `@NotBlank` + `@Pattern` (obrigatório) |

---

## 5. Lógica de Busca Autocomplete

```
GET /api/usuario/buscar?q=joa

1. Valida: q não vazio, min 1 char
2. Normaliza: trim + lowercase
3. Query: findByNomeUsuarioStartingWith(q, Pageable.ofSize(20))
   (busca 20 para ter margem após filtrar amigos)
4. Filtra: remove usuários com FriendshipStatus.ACCEPTED entre requester e resultado
5. Limita: top 10 após filtro
6. Mapeia: UserAccount → UsuarioBuscaResponse (gera fotoPerfilUrl via FileStoragePort)
```

---

## 6. Upload de Foto de Perfil

```
PUT /api/usuario/foto-perfil  (multipart/form-data)

1. Valida: arquivo não vazio
2. Se usuário já tem fotoPerfil key → FileStoragePort.deletar(oldKey)
3. FileStoragePort.salvar(file) → novaKey
4. UserAccountRepositoryPort.save(user com fotoPerfil = novaKey)
5. Retorna FileUploadResponse(novaKey, fileStoragePort.gerarUrlAssinada(novaKey))
```

---

## 7. Tratamento de Erros

| Cenário | Exception | HTTP |
|---------|-----------|------|
| `nomeUsuario` já em uso (registro) | `BusinessException` | 409 |
| `nomeUsuario` formato inválido | Bean Validation | 400 |
| `q` ausente na busca | Bean Validation / `@RequestParam` | 400 |
| Usuário não encontrado | `BusinessException` | 404 |
| Arquivo vazio no upload | `BusinessException` | 400 |

---

## 8. Testes

Cada camada segue o padrão existente (`@ExtendWith(MockitoExtension.class)`, `@Nested` + `@DisplayName`):

- `UserControllerTest` — mock todos os use cases, testa 200/400/404
- `BuscarUsuariosPorPrefixoUsecaseImplTest` — mock repositório e friendship port, verifica filtro e limite
- `AtualizarFotoPerfilUsecaseImplTest` — verifica deleção do arquivo antigo antes de salvar novo
- `UserAccountRepositoryAdapterTest` — mock `AppUserMongoRepository`

---

## 9. Resumo de Arquivos Novos / Alterados

**Alterados:**
- `UserEntity` — campo `fotoPerfil`, remove `sparse` de `nomeUsuario`
- `UserAccount` — campo `fotoPerfil`
- `RegisterRequest` — `nomeUsuario` obrigatório + `@Pattern`
- `AmigoPerfilResponse` — remove `email`, adiciona `fotoPerfilUrl`
- `AmigosConverter` — atualiza mapeamento
- `AppUserMongoRepository` — novos métodos de query

**Novos:**
- `UserController`
- `UsuarioBuscaResponse`, `UsuarioPerfilResponse`, `AtualizarNomeRequest`
- `BuscarUsuariosPorPrefixoUsecase`, `ObterPerfilUsuarioUsecase`, `AtualizarNomeUsuarioUsecase`, `AtualizarFotoPerfilUsecase`, `RemoverFotoPerfilUsecase`
- `BuscarUsuariosPorPrefixoUsecaseImpl`, `ObterPerfilUsuarioUsecaseImpl`, `AtualizarNomeUsuarioUsecaseImpl`, `AtualizarFotoPerfilUsecaseImpl`, `RemoverFotoPerfilUsecaseImpl`
- `UserConverter` (DTO ↔ domain)
- Testes correspondentes

**Já existem (só alterar):**
- `UserAccountRepositoryPort` — adicionar novos métodos, renomear `findByNome` → `findByNomeUsuario`
- `UserAccountRepositoryAdapter` — implementar novos métodos, corrigir `findByNome`
- `AppUserMongoRepository` — corrigir `findByNome` → `findByNomeUsuario`, adicionar métodos de busca
