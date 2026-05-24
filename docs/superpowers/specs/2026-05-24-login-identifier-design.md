# Login com Identifier — Design Spec

**Data:** 2026-05-24
**Escopo:** Alterar login para aceitar email ou nomeUsuario num único campo `identifier`.

---

## Objetivo

Permitir que o usuário faça login com email **ou** nomeUsuario, sem precisar informar qual dos dois está usando.

---

## Abordagem

Campo único `identifier`. Backend detecta o tipo pelo prefixo `@` (estilo Twitter):
- Começa com `@` → nomeUsuario (remove `@`, normaliza, busca por `findByNomeUsuario`)
- Não começa com `@` → email (normaliza lowercase, busca por `findByEmail`)

Exemplos:
- `@usuario_teste` → lookup por `usuario_teste`
- `joao@email.com` → lookup por email

---

## Alterações

### `LoginRequest`

```java
// ANTES
@NotBlank @Email
private String email;

// DEPOIS
@NotBlank
private String identifier;  // email ou nomeUsuario

@NotBlank
private String password;
```

### `LoginUserUsecase`

```java
// Assinatura atualizada
AuthTokens execute(String identifier, String rawPassword);
```

### `LoginUserUsecaseImpl`

Lógica de detecção:

```java
String normalized;
UserAccount user;

if (identifier.startsWith("@")) {
    normalized = NomeUsuarioNormalizer.sanitizePreferencia(identifier.substring(1));
    user = userAccountRepositoryPort.findByNomeUsuario(normalized)
        .orElseThrow(() -> new BusinessException("Credenciais inválidas", HttpStatus.UNAUTHORIZED));
} else {
    normalized = identifier.trim().toLowerCase(Locale.ROOT);
    user = userAccountRepositoryPort.findByEmail(normalized)
        .orElseThrow(() -> new BusinessException("Credenciais inválidas", HttpStatus.UNAUTHORIZED));
}
```

- `LoginAttemptService` usa `normalized` como chave (comportamento idêntico ao atual)
- Mensagem de erro genérica em ambos os fluxos — não vaza qual campo falhou

### Caller (AuthController / LoginRequest binding)

Nenhuma mudança na camada web além do nome do campo no DTO.

---

## Tratamento de erros

| Cenário | Resposta |
|---------|----------|
| identifier vazio/nulo | 400 (Bean Validation `@NotBlank`) |
| email não encontrado | 401 `"Credenciais inválidas"` |
| nomeUsuario não encontrado | 401 `"Credenciais inválidas"` |
| senha incorreta | 401 `"Credenciais inválidas"` |
| conta bloqueada (rate limit) | 429 (comportamento existente) |

---

## Testes

**`LoginUserUsecaseImplTest`** — cenários adicionais:
- Login por `@nomeUsuario` válido → retorna tokens
- Login por `@nomeUsuario` inexistente → 401
- Login por `@nomeUsuario` com `@` + sanitização (ex: `@Usuario_Teste` → lookup `usuario_teste`)
- Login por email continua funcionando (cenários existentes)

---

## Arquivos modificados

| Arquivo | Tipo |
|---------|------|
| `adapters/in/web/dto/request/LoginRequest.java` | Modify |
| `application/ports/in/auth/LoginUserUsecase.java` | Modify |
| `application/service/auth/LoginUserUsecaseImpl.java` | Modify |
| `test/.../auth/LoginUserUsecaseImplTest.java` | Modify |

**Novos arquivos:** nenhum
