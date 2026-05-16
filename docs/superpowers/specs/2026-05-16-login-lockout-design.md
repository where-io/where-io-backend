# Design: Login Lockout após Tentativas Falhas

**Data:** 2026-05-16  
**Status:** Aprovado

## Sumário

Após 3 tentativas de login com senha incorreta, a conta do usuário fica bloqueada por 10 minutos. O estado é mantido em memória (in-process) e não persiste entre restarts do servidor.

## Decisões

| Decisão | Escolha | Motivo |
|---|---|---|
| Persistência do estado | In-memory (`ConcurrentHashMap`) | Restart é aceitável; sem dependências novas |
| Resposta ao bloqueio | `429 Too Many Requests` | Semanticamente correto para rate limiting |
| Reset do contador | Ao expirar 10 min OU login bem-sucedido | Comportamento mais intuitivo |

## Arquitetura

### Novo componente: `LoginAttemptService`

**Localização:** `application/service/auth/LoginAttemptService.java`

Responsabilidade única: rastrear tentativas falhas e lockout por email.

```
ConcurrentHashMap<String, AttemptRecord>
  └── chave: email normalizado (trim + lowercase)
  └── valor: AttemptRecord { int count, Instant lockedUntil }
```

**API pública:**

```java
void recordFailure(String email)
// Incrementa contador. Se count >= 3: define lockedUntil = now + 10 min.

void recordSuccess(String email)
// Remove a entrada (reset completo).

Optional<Duration> getLockoutRemaining(String email)
// Retorna tempo restante se bloqueado; empty se livre.
// Limpa entradas expiradas automaticamente.
```

### Alteração: `LoginUserUsecaseImpl`

Três pontos de integração com `LoginAttemptService`:

1. **Início do execute** → chama `getLockoutRemaining`. Se presente, lança `BusinessException("Conta bloqueada. Tente novamente em X min e Y seg", HttpStatus.TOO_MANY_REQUESTS)`
2. **Após senha inválida** → chama `recordFailure`
3. **Após login bem-sucedido** → chama `recordSuccess`

### Sem alterações em:

- Interface `LoginUserUsecase` — contrato inalterado
- `AuthController` — nenhuma mudança
- `UserEntity` / `UserAccount` — sem novos campos
- `GlobalExceptionHandler` — já trata `BusinessException` genericamente

## Fluxo

```
POST /api/auth/login
  │
  ▼
LoginUserUsecaseImpl.execute()
  │
  ├─ getLockoutRemaining(email)
  │     ├─ bloqueado → throw BusinessException(429) com tempo restante
  │     └─ livre → continua
  │
  ├─ busca usuário no repositório
  │     └─ não encontrado → throw BusinessException(401)
  │
  ├─ verifica senha
  │     ├─ inválida → recordFailure(email) → throw BusinessException(401)
  │     └─ válida → recordSuccess(email) → issueTokens → return AuthTokens
```

## Testes

- `LoginAttemptService` — unitário puro (sem mocks): verificar contagem, lockout ao atingir limite, expiração, reset por sucesso
- `LoginUserUsecaseImpl` — Mockito: verificar chamada a `recordFailure` em senha inválida, `recordSuccess` em sucesso, propagação do 429

## Constantes (hardcoded na implementação)

| Constante | Valor |
|---|---|
| `MAX_ATTEMPTS` | 3 |
| `LOCKOUT_DURATION` | 10 minutos |
