# User & Friend System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add mandatory unique username, user profile photo, username autocomplete search, and profile update endpoints to the where-io backend.

**Architecture:** `UserController` (new) handles all user profile operations under `/api/usuario`. Existing infrastructure (`FileStoragePort`) reuses for profile photo storage. MongoDB prefix-range scan on the `nomeUsuario` index powers autocomplete.

**Tech Stack:** Spring Boot 4, Java 17, MongoDB (Spring Data), MapStruct, Mockito/JUnit 5, Lombok

---

## File Map

**Modified:**
- `src/main/java/analu/whereio/adapters/out/persistence/entity/UserEntity.java`
- `src/main/java/analu/whereio/application/model/UserAccount.java`
- `src/main/java/analu/whereio/adapters/in/web/dto/request/RegisterRequest.java`
- `src/main/java/analu/whereio/adapters/out/persistence/repository/AppUserMongoRepository.java`
- `src/main/java/analu/whereio/application/ports/out/UserAccountRepositoryPort.java`
- `src/main/java/analu/whereio/adapters/out/persistence/impl/UserAccountRepositoryAdapter.java`
- `src/main/java/analu/whereio/application/service/auth/RegisterUserUsecaseImpl.java`
- `src/main/java/analu/whereio/application/service/amigos/EnviarConviteAmizadeUsecaseImpl.java`
- `src/main/java/analu/whereio/adapters/in/web/dto/response/AmigoPerfilResponse.java`
- `src/main/java/analu/whereio/adapters/in/web/converter/AmigosConverter.java`
- `src/main/java/analu/whereio/adapters/in/web/AmigosController.java`
- `src/test/java/analu/whereio/application/service/amigos/EnviarConviteAmizadeUsecaseImplTest.java`

**Created:**
- `src/main/java/analu/whereio/adapters/in/web/dto/response/UsuarioBuscaResponse.java`
- `src/main/java/analu/whereio/adapters/in/web/dto/response/UsuarioPerfilResponse.java`
- `src/main/java/analu/whereio/adapters/in/web/dto/request/AtualizarNomeRequest.java`
- `src/main/java/analu/whereio/application/ports/in/usuario/BuscarUsuariosPorPrefixoUsecase.java`
- `src/main/java/analu/whereio/application/ports/in/usuario/ObterPerfilUsuarioUsecase.java`
- `src/main/java/analu/whereio/application/ports/in/usuario/AtualizarNomeUsuarioUsecase.java`
- `src/main/java/analu/whereio/application/ports/in/usuario/AtualizarFotoPerfilUsecase.java`
- `src/main/java/analu/whereio/application/ports/in/usuario/RemoverFotoPerfilUsecase.java`
- `src/main/java/analu/whereio/application/service/usuario/ObterPerfilUsuarioUsecaseImpl.java`
- `src/main/java/analu/whereio/application/service/usuario/AtualizarNomeUsuarioUsecaseImpl.java`
- `src/main/java/analu/whereio/application/service/usuario/BuscarUsuariosPorPrefixoUsecaseImpl.java`
- `src/main/java/analu/whereio/application/service/usuario/AtualizarFotoPerfilUsecaseImpl.java`
- `src/main/java/analu/whereio/application/service/usuario/RemoverFotoPerfilUsecaseImpl.java`
- `src/main/java/analu/whereio/adapters/in/web/converter/UserConverter.java`
- `src/main/java/analu/whereio/adapters/in/web/UserController.java`
- `src/test/java/analu/whereio/application/service/usuario/ObterPerfilUsuarioUsecaseImplTest.java`
- `src/test/java/analu/whereio/application/service/usuario/AtualizarNomeUsuarioUsecaseImplTest.java`
- `src/test/java/analu/whereio/application/service/usuario/BuscarUsuariosPorPrefixoUsecaseImplTest.java`
- `src/test/java/analu/whereio/application/service/usuario/AtualizarFotoPerfilUsecaseImplTest.java`
- `src/test/java/analu/whereio/application/service/usuario/RemoverFotoPerfilUsecaseImplTest.java`
- `src/test/java/analu/whereio/adapters/in/web/UserControllerTest.java`

---

## Task 1: Update domain models

**Files:**
- Modify: `src/main/java/analu/whereio/adapters/out/persistence/entity/UserEntity.java`
- Modify: `src/main/java/analu/whereio/application/model/UserAccount.java`

- [ ] **Step 1: Update `UserEntity` — add `fotoPerfil`, remove `sparse` from index**

```java
// src/main/java/analu/whereio/adapters/out/persistence/entity/UserEntity.java
package analu.whereio.adapters.out.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "user_table")
public class UserEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;
    private String nome;

    @Indexed(unique = true)
    private String nomeUsuario;

    private String fotoPerfil;

    private List<String> roles = new ArrayList<>(List.of("USER"));
    private Instant createdAt;
}
```

- [ ] **Step 2: Update `UserAccount` — add `fotoPerfil`**

```java
// src/main/java/analu/whereio/application/model/UserAccount.java
package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserAccount {

    private String id;
    private String email;
    private String encodedPassword;
    private String nome;
    private String nomeUsuario;
    private String fotoPerfil;
    private List<String> roles = new ArrayList<>(List.of("USER"));
    private Instant createdAt;
}
```

- [ ] **Step 3: Build to verify compilation**

```bash
./mvnw clean compile -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/analu/whereio/adapters/out/persistence/entity/UserEntity.java
git add src/main/java/analu/whereio/application/model/UserAccount.java
git commit -m "feat: add fotoPerfil field to UserEntity and UserAccount, remove sparse from nomeUsuario index"
```

---

## Task 2: Extend repository layer

**Files:**
- Modify: `src/main/java/analu/whereio/adapters/out/persistence/repository/AppUserMongoRepository.java`
- Modify: `src/main/java/analu/whereio/application/ports/out/UserAccountRepositoryPort.java`
- Modify: `src/main/java/analu/whereio/adapters/out/persistence/impl/UserAccountRepositoryAdapter.java`

- [ ] **Step 1: Update `AppUserMongoRepository` — rename `findByNome` and add new methods**

`findByNome` is a bug: Spring Data generates a query on the `nome` field (display name), not `nomeUsuario`. Rename it and add new query methods.

```java
// src/main/java/analu/whereio/adapters/out/persistence/repository/AppUserMongoRepository.java
package analu.whereio.adapters.out.persistence.repository;

import analu.whereio.adapters.out.persistence.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserMongoRepository extends MongoRepository<UserEntity, String> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByNomeUsuario(String nomeUsuario);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNomeUsuario(String nomeUsuario);

    List<UserEntity> findByNomeUsuarioStartingWithIgnoreCase(String prefix, Pageable pageable);
}
```

- [ ] **Step 2: Update `UserAccountRepositoryPort` — rename `findByNome`, add new methods**

```java
// src/main/java/analu/whereio/application/ports/out/UserAccountRepositoryPort.java
package analu.whereio.application.ports.out;

import analu.whereio.application.model.UserAccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepositoryPort {

    UserAccount save(UserAccount user);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findById(String id);

    List<UserAccount> findAllById(Collection<String> ids);

    Optional<UserAccount> findByNomeUsuario(String nomeUsuario);

    boolean existsByEmail(String email);

    boolean existsByNomeUsuario(String nomeUsuario);

    List<UserAccount> buscarPorPrefixoNomeUsuario(String prefix, int limit);
}
```

- [ ] **Step 3: Update `UserAccountRepositoryAdapter` — implement new methods**

```java
// src/main/java/analu/whereio/adapters/out/persistence/impl/UserAccountRepositoryAdapter.java
package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.entity.UserEntity;
import analu.whereio.adapters.out.persistence.mapper.UserAccountPersistenceMapper;
import analu.whereio.adapters.out.persistence.repository.AppUserMongoRepository;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAccountRepositoryAdapter implements UserAccountRepositoryPort {

    private final AppUserMongoRepository repository;
    private final UserAccountPersistenceMapper mapper;

    @Override
    public UserAccount save(UserAccount user) {
        UserEntity saved = repository.save(mapper.toEntity(user));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }

    @Override
    public Optional<UserAccount> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<UserAccount> findAllById(Collection<String> ids) {
        List<UserEntity> entities = new java.util.ArrayList<>();
        repository.findAllById(ids).forEach(entities::add);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<UserAccount> findByNomeUsuario(String nomeUsuario) {
        return repository.findByNomeUsuario(nomeUsuario).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByNomeUsuario(String nomeUsuario) {
        return repository.existsByNomeUsuario(nomeUsuario);
    }

    @Override
    public List<UserAccount> buscarPorPrefixoNomeUsuario(String prefix, int limit) {
        return repository.findByNomeUsuarioStartingWithIgnoreCase(prefix, PageRequest.of(0, limit))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
```

- [ ] **Step 4: Build to verify compilation**

```bash
./mvnw clean compile -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/analu/whereio/adapters/out/persistence/repository/AppUserMongoRepository.java
git add src/main/java/analu/whereio/application/ports/out/UserAccountRepositoryPort.java
git add src/main/java/analu/whereio/adapters/out/persistence/impl/UserAccountRepositoryAdapter.java
git commit -m "feat: extend repository layer — findByNomeUsuario, existsByNomeUsuario, buscarPorPrefixo"
```

---

## Task 3: Fix auth service, invite service, and their tests

**Files:**
- Modify: `src/main/java/analu/whereio/adapters/in/web/dto/request/RegisterRequest.java`
- Modify: `src/main/java/analu/whereio/application/service/auth/RegisterUserUsecaseImpl.java`
- Modify: `src/main/java/analu/whereio/application/service/amigos/EnviarConviteAmizadeUsecaseImpl.java`
- Modify: `src/test/java/analu/whereio/application/service/amigos/EnviarConviteAmizadeUsecaseImplTest.java`

- [ ] **Step 1: Update `RegisterRequest` — make `nomeUsuario` mandatory with format validation**

```java
// src/main/java/analu/whereio/adapters/in/web/dto/request/RegisterRequest.java
package analu.whereio.adapters.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;

    private String nome;

    @NotBlank(message = "nomeUsuario é obrigatório")
    @Pattern(regexp = "^[a-z0-9_]{3,20}$",
             message = "nomeUsuario deve ter 3–20 caracteres: apenas letras minúsculas, números e _")
    private String nomeUsuario;
}
```

- [ ] **Step 2: Simplify `RegisterUserUsecaseImpl` — remove auto-generation, use `existsByNomeUsuario`**

```java
// src/main/java/analu/whereio/application/service/auth/RegisterUserUsecaseImpl.java
package analu.whereio.application.service.auth;

import analu.whereio.application.model.AuthTokens;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.auth.RegisterUserUsecase;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.application.util.NomeUsuarioNormalizer;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegisterUserUsecaseImpl implements RegisterUserUsecase {

    private final UserAccountRepositoryPort userAccountRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenIssuerService authTokenIssuerService;

    @Override
    public AuthTokens execute(String email, String rawPassword, String nome, String nomeUsuarioRaw) {
        MDC.put("operation", "registerUser");
        try {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (userAccountRepositoryPort.existsByEmail(normalizedEmail)) {
                throw new BusinessException("E-mail já cadastrado", HttpStatus.CONFLICT);
            }
            if (rawPassword == null || rawPassword.length() < 8) {
                throw new BusinessException("Senha deve ter pelo menos 8 caracteres", HttpStatus.BAD_REQUEST);
            }

            String nomeUsuario = resolverNomeUsuario(nomeUsuarioRaw);

            UserAccount account = new UserAccount();
            account.setEmail(normalizedEmail);
            account.setEncodedPassword(passwordEncoder.encode(rawPassword));
            account.setNome(nome != null ? nome.trim() : "");
            account.setNomeUsuario(nomeUsuario);
            account.setCreatedAt(Instant.now());

            UserAccount saved = userAccountRepositoryPort.save(account);
            return authTokenIssuerService.issueForUser(saved);
        } finally {
            MDC.remove("operation");
        }
    }

    private String resolverNomeUsuario(String nomeUsuarioRaw) {
        if (nomeUsuarioRaw == null || nomeUsuarioRaw.isBlank()) {
            throw new BusinessException("nomeUsuario é obrigatório", HttpStatus.BAD_REQUEST);
        }
        String sanitized = NomeUsuarioNormalizer.sanitizePreferencia(nomeUsuarioRaw);
        if (sanitized.length() < 3) {
            throw new BusinessException("Nome de usuário inválido", HttpStatus.BAD_REQUEST);
        }
        if (userAccountRepositoryPort.existsByNomeUsuario(sanitized)) {
            throw new BusinessException("Nome de usuário já está em uso", HttpStatus.CONFLICT);
        }
        return sanitized;
    }
}
```

- [ ] **Step 3: Fix `EnviarConviteAmizadeUsecaseImpl` — replace `findByNome` with `findByNomeUsuario`**

Replace line 38 only:

```java
// src/main/java/analu/whereio/application/service/amigos/EnviarConviteAmizadeUsecaseImpl.java
// Change:
UserAccount destinatarioConta = userAccountRepositoryPort.findByNome(nomeUsuarioDestinatario)
// To:
UserAccount destinatarioConta = userAccountRepositoryPort.findByNomeUsuario(nomeUsuarioDestinatario)
```

Full updated file:

```java
package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.amigos.EnviarConviteAmizadeUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class EnviarConviteAmizadeUsecaseImpl implements EnviarConviteAmizadeUsecase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public Friendship execute(String requesterUserId, String nomeUsuarioDestinatario) {
        MDC.put("operation", "enviarConviteAmizade");
        try {
            String solicitante = requesterUserId != null ? requesterUserId.trim() : "";

            if (solicitante.isBlank()) {
                throw new BusinessException("Usuário solicitante inválido", HttpStatus.BAD_REQUEST);
            }
            if (nomeUsuarioDestinatario == null || nomeUsuarioDestinatario.length() < 3) {
                throw new BusinessException("Nome de usuário inválido", HttpStatus.BAD_REQUEST);
            }

            UserAccount destinatarioConta = userAccountRepositoryPort.findByNomeUsuario(nomeUsuarioDestinatario)
                    .orElseThrow(() -> new BusinessException(
                            "Nenhum usuário encontrado com este nome de usuário.", HttpStatus.NOT_FOUND));

            String destinatario = destinatarioConta.getId();
            if (solicitante.equals(destinatario)) {
                throw new BusinessException("Não é possível convidar a si mesmo", HttpStatus.BAD_REQUEST);
            }

            var existentes = friendshipRepositoryPort.findAllBetweenUsers(solicitante, destinatario);
            for (Friendship f : existentes) {
                if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                    throw new BusinessException("Vocês já são amigos", HttpStatus.CONFLICT);
                }
                if (f.getStatus() == FriendshipStatus.PENDING) {
                    throw new BusinessException("Já existe um convite pendente entre vocês", HttpStatus.CONFLICT);
                }
            }

            Friendship novo = new Friendship();
            novo.setRequesterUserId(solicitante);
            novo.setAddresseeUserId(destinatario);
            novo.setStatus(FriendshipStatus.PENDING);
            novo.setCreatedAt(Instant.now());

            Friendship salvo = friendshipRepositoryPort.save(novo);
            MDC.put("entityId", salvo.getId());
            return salvo;
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
```

- [ ] **Step 4: Update `EnviarConviteAmizadeUsecaseImplTest` — replace all `findByNome` with `findByNomeUsuario`**

```java
// src/test/java/analu/whereio/application/service/amigos/EnviarConviteAmizadeUsecaseImplTest.java
package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnviarConviteAmizadeUsecaseImpl")
class EnviarConviteAmizadeUsecaseImplTest {

    private static final String USER_A = "user-a";
    private static final String USER_B = "user-b";

    @Mock
    private FriendshipRepositoryPort friendshipRepositoryPort;

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private EnviarConviteAmizadeUsecaseImpl enviarConviteAmizadeUsecase;

    @Nested
    @DisplayName("Validações")
    class Validacoes {

        @Test
        @DisplayName("deve lançar BAD_REQUEST ao convidar a si mesmo")
        void naoPodeConvidarSiMesmo() {
            when(userAccountRepositoryPort.findByNomeUsuario(eq("user-a")))
                    .thenReturn(Optional.of(conta(USER_A, "user-a")));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> enviarConviteAmizadeUsecase.execute(USER_A, "user-a"));

            assertAll(
                    () -> assertEquals("Não é possível convidar a si mesmo", ex.getMessage()),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus())
            );
            verify(userAccountRepositoryPort).findByNomeUsuario("user-a");
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando o destinatário não existe")
        void destinatarioDeveExistir() {
            when(userAccountRepositoryPort.findByNomeUsuario(eq("user-b"))).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> enviarConviteAmizadeUsecase.execute(USER_A, "user-b"));

            assertAll(
                    () -> assertEquals(HttpStatus.NOT_FOUND, ex.getStatus()),
                    () -> assertEquals("Nenhum usuário encontrado com este nome de usuário.", ex.getMessage())
            );
            verify(friendshipRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar CONFLICT quando já são amigos")
        void jaSaoAmigos() {
            when(userAccountRepositoryPort.findByNomeUsuario(eq("user-b")))
                    .thenReturn(Optional.of(conta(USER_B, "user-b")));

            Friendship aceito = new Friendship();
            aceito.setStatus(FriendshipStatus.ACCEPTED);
            when(friendshipRepositoryPort.findAllBetweenUsers(USER_A, USER_B)).thenReturn(List.of(aceito));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> enviarConviteAmizadeUsecase.execute(USER_A, "user-b"));

            assertEquals(HttpStatus.CONFLICT, ex.getStatus());
            verify(friendshipRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar CONFLICT quando já há convite pendente")
        void convitePendente() {
            when(userAccountRepositoryPort.findByNomeUsuario(eq("user-b")))
                    .thenReturn(Optional.of(conta(USER_B, "user-b")));

            Friendship pendente = new Friendship();
            pendente.setStatus(FriendshipStatus.PENDING);
            when(friendshipRepositoryPort.findAllBetweenUsers(USER_A, USER_B)).thenReturn(List.of(pendente));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> enviarConviteAmizadeUsecase.execute(USER_A, "user-b"));

            assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        }
    }

    @Nested
    @DisplayName("Fluxo feliz")
    class FluxoFeliz {

        @Test
        @DisplayName("deve persistir convite PENDING")
        void persisteConvite() {
            when(userAccountRepositoryPort.findByNomeUsuario(eq("user-b")))
                    .thenReturn(Optional.of(conta(USER_B, "user-b")));
            when(friendshipRepositoryPort.findAllBetweenUsers(USER_A, USER_B)).thenReturn(List.of());
            when(friendshipRepositoryPort.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
            Friendship resultado = enviarConviteAmizadeUsecase.execute(USER_A, "user-b");

            verify(friendshipRepositoryPort).save(captor.capture());
            assertAll(
                    () -> assertEquals(FriendshipStatus.PENDING, captor.getValue().getStatus()),
                    () -> assertEquals(USER_A, captor.getValue().getRequesterUserId()),
                    () -> assertEquals(USER_B, captor.getValue().getAddresseeUserId()),
                    () -> assertEquals(FriendshipStatus.PENDING, resultado.getStatus())
            );
        }
    }

    private static UserAccount conta(String id, String nomeUsuario) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNome(nomeUsuario);
        u.setNomeUsuario(nomeUsuario);
        u.setEmail(id + "@t.test");
        return u;
    }
}
```

- [ ] **Step 5: Run tests**

```bash
./mvnw test -Dtest="EnviarConviteAmizadeUsecaseImplTest,LoginUserUsecaseImplTest" -q
```

Expected: `BUILD SUCCESS`, all tests green.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/analu/whereio/adapters/in/web/dto/request/RegisterRequest.java
git add src/main/java/analu/whereio/application/service/auth/RegisterUserUsecaseImpl.java
git add src/main/java/analu/whereio/application/service/amigos/EnviarConviteAmizadeUsecaseImpl.java
git add src/test/java/analu/whereio/application/service/amigos/EnviarConviteAmizadeUsecaseImplTest.java
git commit -m "fix: nomeUsuario mandatory in register, findByNomeUsuario replaces findByNome bug"
```

---

## Task 4: New DTOs, update AmigoPerfilResponse + AmigosConverter + AmigosController

**Files:**
- Create: `src/main/java/analu/whereio/adapters/in/web/dto/response/UsuarioBuscaResponse.java`
- Create: `src/main/java/analu/whereio/adapters/in/web/dto/response/UsuarioPerfilResponse.java`
- Create: `src/main/java/analu/whereio/adapters/in/web/dto/request/AtualizarNomeRequest.java`
- Modify: `src/main/java/analu/whereio/adapters/in/web/dto/response/AmigoPerfilResponse.java`
- Modify: `src/main/java/analu/whereio/adapters/in/web/converter/AmigosConverter.java`
- Modify: `src/main/java/analu/whereio/adapters/in/web/AmigosController.java`

- [ ] **Step 1: Create `UsuarioBuscaResponse`**

```java
// src/main/java/analu/whereio/adapters/in/web/dto/response/UsuarioBuscaResponse.java
package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioBuscaResponse {

    private String nomeUsuario;
    private String nome;
    private String fotoPerfilUrl;
}
```

- [ ] **Step 2: Create `UsuarioPerfilResponse`**

```java
// src/main/java/analu/whereio/adapters/in/web/dto/response/UsuarioPerfilResponse.java
package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioPerfilResponse {

    private String id;
    private String nome;
    private String nomeUsuario;
    private String email;
    private String fotoPerfilUrl;
}
```

- [ ] **Step 3: Create `AtualizarNomeRequest`**

```java
// src/main/java/analu/whereio/adapters/in/web/dto/request/AtualizarNomeRequest.java
package analu.whereio.adapters.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarNomeRequest {

    @NotBlank(message = "nome é obrigatório")
    @Size(min = 2, max = 80, message = "nome deve ter entre 2 e 80 caracteres")
    private String nome;
}
```

- [ ] **Step 4: Update `AmigoPerfilResponse` — remove `email`, add `fotoPerfilUrl`**

```java
// src/main/java/analu/whereio/adapters/in/web/dto/response/AmigoPerfilResponse.java
package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmigoPerfilResponse {

    private String id;
    private String nomeUsuario;
    private String nome;
    private String fotoPerfilUrl;
}
```

- [ ] **Step 5: Update `AmigosConverter` — remove `email` mapping, ignore `fotoPerfilUrl`**

```java
// src/main/java/analu/whereio/adapters/in/web/converter/AmigosConverter.java
package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.response.AmigoPerfilResponse;
import analu.whereio.adapters.in.web.dto.response.AmizadeConviteResponse;
import analu.whereio.adapters.in.web.dto.response.ConviteEnviadoResponse;
import analu.whereio.application.model.ConviteAmizadeResumoItem;
import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AmigosConverter {

    @Mapping(source = "requesterUserId", target = "solicitanteId")
    @Mapping(source = "addresseeUserId", target = "destinatarioId")
    @Mapping(source = "createdAt", target = "criadoEm")
    @Mapping(source = "acceptedAt", target = "aceitoEm")
    @Mapping(source = "status", target = "status", qualifiedByName = "friendshipStatusToString")
    AmizadeConviteResponse toConviteResponse(Friendship friendship);

    @Mapping(source = "createdAt", target = "criadoEm")
    @Mapping(source = "acceptedAt", target = "aceitoEm")
    @Mapping(source = "status", target = "status", qualifiedByName = "friendshipStatusToString")
    ConviteEnviadoResponse toConviteEnviadoResponse(ConviteAmizadeResumoItem item);

    @Named("friendshipStatusToString")
    default String friendshipStatusToString(FriendshipStatus status) {
        return status == null ? null : status.name();
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nomeUsuario", target = "nomeUsuario")
    @Mapping(source = "nome", target = "nome")
    @Mapping(target = "fotoPerfilUrl", ignore = true)
    AmigoPerfilResponse toAmigoResponse(UserAccount account);
}
```

- [ ] **Step 6: Update `AmigosController` — inject `FileStoragePort`, enrich `fotoPerfilUrl` in `listarAmigos`**

```java
// src/main/java/analu/whereio/adapters/in/web/AmigosController.java
package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.AmigosConverter;
import analu.whereio.adapters.in.web.dto.request.EnviarConviteAmizadeRequest;
import analu.whereio.adapters.in.web.dto.response.AmigoPerfilResponse;
import analu.whereio.adapters.in.web.dto.response.AmizadeConviteResponse;
import analu.whereio.adapters.in.web.dto.response.ConviteEnviadoResponse;
import analu.whereio.adapters.in.web.dto.response.EnviarConviteAmizadeResponse;
import analu.whereio.application.model.Friendship;
import analu.whereio.application.ports.in.amigos.AceitarConviteAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.EnviarConviteAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.ListarAmigosUsecase;
import analu.whereio.application.ports.in.amigos.ListarConvitesEnviadosAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.ListarConvitesRecebidosAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.RecusarOuCancelarConviteAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.RemoverAmigoUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.config.security.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amigos")
@RequiredArgsConstructor
public class AmigosController {

    private static final Logger log = LoggerFactory.getLogger(AmigosController.class);

    private final EnviarConviteAmizadeUsecase enviarConviteAmizadeUsecase;
    private final AceitarConviteAmizadeUsecase aceitarConviteAmizadeUsecase;
    private final ListarConvitesRecebidosAmizadeUsecase listarConvitesRecebidosAmizadeUsecase;
    private final ListarConvitesEnviadosAmizadeUsecase listarConvitesEnviadosAmizadeUsecase;
    private final ListarAmigosUsecase listarAmigosUsecase;
    private final RecusarOuCancelarConviteAmizadeUsecase recusarOuCancelarConviteAmizadeUsecase;
    private final RemoverAmigoUsecase removerAmigoUsecase;
    private final AmigosConverter converter;
    private final FileStoragePort fileStoragePort;

    @PostMapping("/convites")
    ResponseEntity<EnviarConviteAmizadeResponse> enviarConvite(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody EnviarConviteAmizadeRequest body) {
        Friendship criado = enviarConviteAmizadeUsecase.execute(
                principal.getUserId(), body.getNomeUsuarioDestino());
        EnviarConviteAmizadeResponse resposta = new EnviarConviteAmizadeResponse();
        resposta.setSucesso(true);
        resposta.setMensagem("Convite enviado com sucesso.");
        resposta.setConvite(converter.toConviteResponse(criado));
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/convites/{id}/aceitar")
    ResponseEntity<AmizadeConviteResponse> aceitarConvite(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        Friendship atualizado = aceitarConviteAmizadeUsecase.execute(id, principal.getUserId());
        return ResponseEntity.ok(converter.toConviteResponse(atualizado));
    }

    @DeleteMapping("/convites/{id}")
    ResponseEntity<Void> recusarOuCancelarConvite(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        recusarOuCancelarConviteAmizadeUsecase.execute(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/convites/recebidos")
    ResponseEntity<List<ConviteEnviadoResponse>> listarRecebidos(@AuthenticationPrincipal JwtUserPrincipal principal) {
        List<ConviteEnviadoResponse> lista = listarConvitesRecebidosAmizadeUsecase.execute(principal.getUserId()).stream()
                .map(converter::toConviteEnviadoResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/convites/enviados")
    ResponseEntity<List<ConviteEnviadoResponse>> listarEnviados(@AuthenticationPrincipal JwtUserPrincipal principal) {
        List<ConviteEnviadoResponse> lista = listarConvitesEnviadosAmizadeUsecase.execute(principal.getUserId()).stream()
                .map(converter::toConviteEnviadoResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> removerAmigo(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String id) {
        removerAmigoUsecase.execute(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    ResponseEntity<List<AmigoPerfilResponse>> listarAmigos(@AuthenticationPrincipal JwtUserPrincipal principal) {
        List<AmigoPerfilResponse> lista = listarAmigosUsecase.execute(principal.getUserId()).stream()
                .map(account -> {
                    AmigoPerfilResponse resp = converter.toAmigoResponse(account);
                    if (account.getFotoPerfil() != null) {
                        resp.setFotoPerfilUrl(fileStoragePort.gerarUrlAssinada(account.getFotoPerfil()));
                    }
                    return resp;
                })
                .toList();
        return ResponseEntity.ok(lista);
    }
}
```

- [ ] **Step 7: Build and run full test suite**

```bash
./mvnw test -q
```

Expected: `BUILD SUCCESS`, all tests green.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/analu/whereio/adapters/in/web/dto/response/UsuarioBuscaResponse.java
git add src/main/java/analu/whereio/adapters/in/web/dto/response/UsuarioPerfilResponse.java
git add src/main/java/analu/whereio/adapters/in/web/dto/request/AtualizarNomeRequest.java
git add src/main/java/analu/whereio/adapters/in/web/dto/response/AmigoPerfilResponse.java
git add src/main/java/analu/whereio/adapters/in/web/converter/AmigosConverter.java
git add src/main/java/analu/whereio/adapters/in/web/AmigosController.java
git commit -m "feat: new user DTOs, remove email from AmigoPerfilResponse, add fotoPerfilUrl"
```

---

## Task 5: Use case interfaces

**Files:** 5 new interfaces in `src/main/java/analu/whereio/application/ports/in/usuario/`

- [ ] **Step 1: Create `BuscarUsuariosPorPrefixoUsecase`**

```java
// src/main/java/analu/whereio/application/ports/in/usuario/BuscarUsuariosPorPrefixoUsecase.java
package analu.whereio.application.ports.in.usuario;

import analu.whereio.application.model.UserAccount;

import java.util.List;

public interface BuscarUsuariosPorPrefixoUsecase {
    List<UserAccount> execute(String prefix, String requesterId);
}
```

- [ ] **Step 2: Create `ObterPerfilUsuarioUsecase`**

```java
// src/main/java/analu/whereio/application/ports/in/usuario/ObterPerfilUsuarioUsecase.java
package analu.whereio.application.ports.in.usuario;

import analu.whereio.application.model.UserAccount;

public interface ObterPerfilUsuarioUsecase {
    UserAccount execute(String userId);
}
```

- [ ] **Step 3: Create `AtualizarNomeUsuarioUsecase`**

```java
// src/main/java/analu/whereio/application/ports/in/usuario/AtualizarNomeUsuarioUsecase.java
package analu.whereio.application.ports.in.usuario;

import analu.whereio.application.model.UserAccount;

public interface AtualizarNomeUsuarioUsecase {
    UserAccount execute(String userId, String novoNome);
}
```

- [ ] **Step 4: Create `AtualizarFotoPerfilUsecase`**

```java
// src/main/java/analu/whereio/application/ports/in/usuario/AtualizarFotoPerfilUsecase.java
package analu.whereio.application.ports.in.usuario;

import org.springframework.web.multipart.MultipartFile;

public interface AtualizarFotoPerfilUsecase {
    String execute(MultipartFile file, String userId);
}
```

- [ ] **Step 5: Create `RemoverFotoPerfilUsecase`**

```java
// src/main/java/analu/whereio/application/ports/in/usuario/RemoverFotoPerfilUsecase.java
package analu.whereio.application.ports.in.usuario;

public interface RemoverFotoPerfilUsecase {
    void execute(String userId);
}
```

- [ ] **Step 6: Build to verify**

```bash
./mvnw clean compile -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```bash
git add src/main/java/analu/whereio/application/ports/in/usuario/
git commit -m "feat: add use case interfaces for user profile operations"
```

---

## Task 6: ObterPerfilUsuarioUsecaseImpl + AtualizarNomeUsuarioUsecaseImpl + tests

**Files:**
- Create: `src/main/java/analu/whereio/application/service/usuario/ObterPerfilUsuarioUsecaseImpl.java`
- Create: `src/main/java/analu/whereio/application/service/usuario/AtualizarNomeUsuarioUsecaseImpl.java`
- Create: `src/test/java/analu/whereio/application/service/usuario/ObterPerfilUsuarioUsecaseImplTest.java`
- Create: `src/test/java/analu/whereio/application/service/usuario/AtualizarNomeUsuarioUsecaseImplTest.java`

- [ ] **Step 1: Write failing tests for `ObterPerfilUsuarioUsecaseImpl`**

```java
// src/test/java/analu/whereio/application/service/usuario/ObterPerfilUsuarioUsecaseImplTest.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ObterPerfilUsuarioUsecaseImpl")
class ObterPerfilUsuarioUsecaseImplTest {

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private ObterPerfilUsuarioUsecaseImpl usecase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deve retornar UserAccount quando usuário existe")
        void deveRetornarUserAccountQuandoExiste() {
            UserAccount conta = conta("user-1", "joao123");
            when(userAccountRepositoryPort.findById("user-1")).thenReturn(Optional.of(conta));

            UserAccount resultado = usecase.execute("user-1");

            assertEquals("user-1", resultado.getId());
            assertEquals("joao123", resultado.getNomeUsuario());
            verify(userAccountRepositoryPort).findById("user-1");
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando usuário não existe")
        void deveLancarNotFoundQuandoNaoExiste() {
            when(userAccountRepositoryPort.findById("inexistente")).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute("inexistente"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }
    }

    private static UserAccount conta(String id, String nomeUsuario) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNomeUsuario(nomeUsuario);
        u.setNome("João Silva");
        u.setEmail(id + "@test.local");
        return u;
    }
}
```

- [ ] **Step 2: Write failing tests for `AtualizarNomeUsuarioUsecaseImpl`**

```java
// src/test/java/analu/whereio/application/service/usuario/AtualizarNomeUsuarioUsecaseImplTest.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarNomeUsuarioUsecaseImpl")
class AtualizarNomeUsuarioUsecaseImplTest {

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private AtualizarNomeUsuarioUsecaseImpl usecase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deve atualizar nome e persistir")
        void deveAtualizarNomeEPersistir() {
            UserAccount conta = conta("user-1");
            when(userAccountRepositoryPort.findById("user-1")).thenReturn(Optional.of(conta));
            when(userAccountRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserAccount resultado = usecase.execute("user-1", "Novo Nome");

            ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountRepositoryPort).save(captor.capture());
            assertEquals("Novo Nome", captor.getValue().getNome());
            assertEquals("Novo Nome", resultado.getNome());
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando usuário não existe")
        void deveLancarNotFoundQuandoNaoExiste() {
            when(userAccountRepositoryPort.findById("x")).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute("x", "Nome"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
            verify(userAccountRepositoryPort, never()).save(any());
        }
    }

    private static UserAccount conta(String id) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNomeUsuario("user123");
        u.setNome("Nome Antigo");
        u.setEmail(id + "@test.local");
        return u;
    }
}
```

- [ ] **Step 3: Run tests to verify they FAIL**

```bash
./mvnw test -Dtest="ObterPerfilUsuarioUsecaseImplTest,AtualizarNomeUsuarioUsecaseImplTest" -q 2>&1 | tail -5
```

Expected: `BUILD FAILURE` — classes do not exist yet.

- [ ] **Step 4: Implement `ObterPerfilUsuarioUsecaseImpl`**

```java
// src/main/java/analu/whereio/application/service/usuario/ObterPerfilUsuarioUsecaseImpl.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.ObterPerfilUsuarioUsecase;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObterPerfilUsuarioUsecaseImpl implements ObterPerfilUsuarioUsecase {

    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public UserAccount execute(String userId) {
        MDC.put("operation", "obterPerfilUsuario");
        try {
            return userAccountRepositoryPort.findById(userId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));
        } finally {
            MDC.remove("operation");
        }
    }
}
```

- [ ] **Step 5: Implement `AtualizarNomeUsuarioUsecaseImpl`**

```java
// src/main/java/analu/whereio/application/service/usuario/AtualizarNomeUsuarioUsecaseImpl.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.AtualizarNomeUsuarioUsecase;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AtualizarNomeUsuarioUsecaseImpl implements AtualizarNomeUsuarioUsecase {

    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public UserAccount execute(String userId, String novoNome) {
        MDC.put("operation", "atualizarNomeUsuario");
        MDC.put("entityId", userId);
        try {
            UserAccount account = userAccountRepositoryPort.findById(userId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));
            account.setNome(novoNome.trim());
            return userAccountRepositoryPort.save(account);
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
```

- [ ] **Step 6: Run tests — verify they PASS**

```bash
./mvnw test -Dtest="ObterPerfilUsuarioUsecaseImplTest,AtualizarNomeUsuarioUsecaseImplTest" -q
```

Expected: `BUILD SUCCESS`, 3 tests green.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/analu/whereio/application/service/usuario/ObterPerfilUsuarioUsecaseImpl.java
git add src/main/java/analu/whereio/application/service/usuario/AtualizarNomeUsuarioUsecaseImpl.java
git add src/test/java/analu/whereio/application/service/usuario/ObterPerfilUsuarioUsecaseImplTest.java
git add src/test/java/analu/whereio/application/service/usuario/AtualizarNomeUsuarioUsecaseImplTest.java
git commit -m "feat: ObterPerfilUsuarioUsecaseImpl and AtualizarNomeUsuarioUsecaseImpl"
```

---

## Task 7: BuscarUsuariosPorPrefixoUsecaseImpl + test

**Files:**
- Create: `src/main/java/analu/whereio/application/service/usuario/BuscarUsuariosPorPrefixoUsecaseImpl.java`
- Create: `src/test/java/analu/whereio/application/service/usuario/BuscarUsuariosPorPrefixoUsecaseImplTest.java`

**Logic:** query 20 candidates by prefix → filter out self + confirmed friends → return top 10.

- [ ] **Step 1: Write failing test**

```java
// src/test/java/analu/whereio/application/service/usuario/BuscarUsuariosPorPrefixoUsecaseImplTest.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarUsuariosPorPrefixoUsecaseImpl")
class BuscarUsuariosPorPrefixoUsecaseImplTest {

    private static final String REQUESTER_ID = "requester-1";

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @Mock
    private FriendshipRepositoryPort friendshipRepositoryPort;

    @InjectMocks
    private BuscarUsuariosPorPrefixoUsecaseImpl usecase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deve retornar usuários que começam com o prefixo, excluindo o próprio requester")
        void deveExcluirOProprioPerfil() {
            UserAccount self = conta(REQUESTER_ID, "joa_req");
            UserAccount other = conta("other-1", "joao123");
            when(userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(eq("joa"), eq(20)))
                    .thenReturn(List.of(self, other));
            when(friendshipRepositoryPort.findAcceptedForUser(REQUESTER_ID)).thenReturn(List.of());

            List<UserAccount> resultado = usecase.execute("joa", REQUESTER_ID);

            assertEquals(1, resultado.size());
            assertEquals("joao123", resultado.get(0).getNomeUsuario());
        }

        @Test
        @DisplayName("deve excluir amigos confirmados")
        void deveExcluirAmigosConfirmados() {
            UserAccount amigo = conta("amigo-1", "joao_amigo");
            UserAccount estranho = conta("estranho-1", "joana_x");
            when(userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(eq("joa"), eq(20)))
                    .thenReturn(List.of(amigo, estranho));

            Friendship amizade = new Friendship();
            amizade.setRequesterUserId(REQUESTER_ID);
            amizade.setAddresseeUserId("amigo-1");
            amizade.setStatus(FriendshipStatus.ACCEPTED);
            when(friendshipRepositoryPort.findAcceptedForUser(REQUESTER_ID)).thenReturn(List.of(amizade));

            List<UserAccount> resultado = usecase.execute("joa", REQUESTER_ID);

            assertEquals(1, resultado.size());
            assertEquals("joana_x", resultado.get(0).getNomeUsuario());
        }

        @Test
        @DisplayName("deve limitar resultados a 10")
        void deveLimitarA10() {
            List<UserAccount> candidates = new java.util.ArrayList<>();
            for (int i = 0; i < 20; i++) {
                candidates.add(conta("id-" + i, "joao" + i));
            }
            when(userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(eq("joa"), eq(20)))
                    .thenReturn(candidates);
            when(friendshipRepositoryPort.findAcceptedForUser(REQUESTER_ID)).thenReturn(List.of());

            List<UserAccount> resultado = usecase.execute("joa", REQUESTER_ID);

            assertEquals(10, resultado.size());
        }

        @Test
        @DisplayName("deve normalizar prefixo para lowercase antes da query")
        void deveNormalizarPrefixoParaLowercase() {
            when(userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(eq("joa"), eq(20)))
                    .thenReturn(List.of());
            when(friendshipRepositoryPort.findAcceptedForUser(REQUESTER_ID)).thenReturn(List.of());

            usecase.execute("JOA", REQUESTER_ID);

            verify(userAccountRepositoryPort).buscarPorPrefixoNomeUsuario("joa", 20);
        }
    }

    private static UserAccount conta(String id, String nomeUsuario) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNomeUsuario(nomeUsuario);
        u.setNome("Nome " + id);
        return u;
    }
}
```

- [ ] **Step 2: Run test to verify FAIL**

```bash
./mvnw test -Dtest="BuscarUsuariosPorPrefixoUsecaseImplTest" -q 2>&1 | tail -5
```

Expected: `BUILD FAILURE`

- [ ] **Step 3: Implement `BuscarUsuariosPorPrefixoUsecaseImpl`**

```java
// src/main/java/analu/whereio/application/service/usuario/BuscarUsuariosPorPrefixoUsecaseImpl.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.BuscarUsuariosPorPrefixoUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BuscarUsuariosPorPrefixoUsecaseImpl implements BuscarUsuariosPorPrefixoUsecase {

    private static final int CANDIDATE_POOL = 20;
    private static final int MAX_RESULTS = 10;

    private final UserAccountRepositoryPort userAccountRepositoryPort;
    private final FriendshipRepositoryPort friendshipRepositoryPort;

    @Override
    public List<UserAccount> execute(String prefix, String requesterId) {
        MDC.put("operation", "buscarUsuariosPorPrefixo");
        try {
            String normalizedPrefix = prefix.trim().toLowerCase(Locale.ROOT);

            Set<String> confirmedFriendIds = friendshipRepositoryPort.findAcceptedForUser(requesterId)
                    .stream()
                    .map(f -> friendId(f, requesterId))
                    .collect(Collectors.toSet());

            return userAccountRepositoryPort.buscarPorPrefixoNomeUsuario(normalizedPrefix, CANDIDATE_POOL)
                    .stream()
                    .filter(u -> !u.getId().equals(requesterId))
                    .filter(u -> !confirmedFriendIds.contains(u.getId()))
                    .limit(MAX_RESULTS)
                    .toList();
        } finally {
            MDC.remove("operation");
        }
    }

    private String friendId(Friendship friendship, String requesterId) {
        return friendship.getRequesterUserId().equals(requesterId)
                ? friendship.getAddresseeUserId()
                : friendship.getRequesterUserId();
    }
}
```

- [ ] **Step 4: Run tests — verify PASS**

```bash
./mvnw test -Dtest="BuscarUsuariosPorPrefixoUsecaseImplTest" -q
```

Expected: `BUILD SUCCESS`, 4 tests green.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/analu/whereio/application/service/usuario/BuscarUsuariosPorPrefixoUsecaseImpl.java
git add src/test/java/analu/whereio/application/service/usuario/BuscarUsuariosPorPrefixoUsecaseImplTest.java
git commit -m "feat: BuscarUsuariosPorPrefixoUsecaseImpl with friend exclusion and 10-result limit"
```

---

## Task 8: AtualizarFotoPerfilUsecaseImpl + RemoverFotoPerfilUsecaseImpl + tests

**Files:**
- Create: `src/main/java/analu/whereio/application/service/usuario/AtualizarFotoPerfilUsecaseImpl.java`
- Create: `src/main/java/analu/whereio/application/service/usuario/RemoverFotoPerfilUsecaseImpl.java`
- Create: `src/test/java/analu/whereio/application/service/usuario/AtualizarFotoPerfilUsecaseImplTest.java`
- Create: `src/test/java/analu/whereio/application/service/usuario/RemoverFotoPerfilUsecaseImplTest.java`

- [ ] **Step 1: Write failing tests**

```java
// src/test/java/analu/whereio/application/service/usuario/AtualizarFotoPerfilUsecaseImplTest.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarFotoPerfilUsecaseImpl")
class AtualizarFotoPerfilUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private AtualizarFotoPerfilUsecaseImpl usecase;

    private MultipartFile arquivoValido() {
        return new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Nested
    @DisplayName("Validação")
    class Validacao {

        @Test
        @DisplayName("deve lançar BAD_REQUEST para arquivo vazio")
        void arquivoVazio() {
            MultipartFile vazio = new MockMultipartFile("file", "x.jpg", "image/jpeg", new byte[]{});

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(vazio, USER_ID));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            verify(fileStoragePort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND para usuário inexistente")
        void usuarioInexistente() {
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(arquivoValido(), USER_ID));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }
    }

    @Nested
    @DisplayName("Sucesso")
    class Sucesso {

        @Test
        @DisplayName("deve salvar arquivo e persistir key no usuário")
        void deveSalvarArquivoEPersistirKey() throws Exception {
            UserAccount conta = conta(USER_ID, null);
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(conta));
            when(fileStoragePort.salvar(any())).thenReturn("nova_foto.jpg");
            when(userAccountRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            String key = usecase.execute(arquivoValido(), USER_ID);

            assertEquals("nova_foto.jpg", key);
            ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountRepositoryPort).save(captor.capture());
            assertEquals("nova_foto.jpg", captor.getValue().getFotoPerfil());
        }

        @Test
        @DisplayName("deve deletar foto antiga antes de salvar nova")
        void deveDeletarFotoAntigaAntesDeSalvarNova() throws Exception {
            UserAccount conta = conta(USER_ID, "foto_antiga.jpg");
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(conta));
            when(fileStoragePort.salvar(any())).thenReturn("nova_foto.jpg");
            when(userAccountRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            usecase.execute(arquivoValido(), USER_ID);

            verify(fileStoragePort).deletar("foto_antiga.jpg");
            verify(fileStoragePort).salvar(any());
        }
    }

    private static UserAccount conta(String id, String fotoPerfil) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNomeUsuario("user123");
        u.setFotoPerfil(fotoPerfil);
        return u;
    }
}
```

```java
// src/test/java/analu/whereio/application/service/usuario/RemoverFotoPerfilUsecaseImplTest.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoverFotoPerfilUsecaseImpl")
class RemoverFotoPerfilUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private RemoverFotoPerfilUsecaseImpl usecase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deve deletar arquivo e limpar campo fotoPerfil")
        void deveDeletarArquivoELimparCampo() throws Exception {
            UserAccount conta = conta(USER_ID, "foto.jpg");
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(conta));
            when(userAccountRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            usecase.execute(USER_ID);

            verify(fileStoragePort).deletar("foto.jpg");
            ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountRepositoryPort).save(captor.capture());
            assertNull(captor.getValue().getFotoPerfil());
        }

        @Test
        @DisplayName("deve ser no-op quando usuário não tem foto")
        void deveSerNoOpSemFoto() throws Exception {
            UserAccount conta = conta(USER_ID, null);
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(conta));

            usecase.execute(USER_ID);

            verify(fileStoragePort, never()).deletar(any());
            verify(userAccountRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND para usuário inexistente")
        void deveLancarNotFoundParaUsuarioInexistente() {
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(USER_ID));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }
    }

    private static UserAccount conta(String id, String fotoPerfil) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNomeUsuario("user123");
        u.setFotoPerfil(fotoPerfil);
        return u;
    }
}
```

- [ ] **Step 2: Run tests to verify FAIL**

```bash
./mvnw test -Dtest="AtualizarFotoPerfilUsecaseImplTest,RemoverFotoPerfilUsecaseImplTest" -q 2>&1 | tail -5
```

Expected: `BUILD FAILURE`

- [ ] **Step 3: Implement `AtualizarFotoPerfilUsecaseImpl`**

```java
// src/main/java/analu/whereio/application/service/usuario/AtualizarFotoPerfilUsecaseImpl.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.AtualizarFotoPerfilUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AtualizarFotoPerfilUsecaseImpl implements AtualizarFotoPerfilUsecase {

    private static final Logger log = LoggerFactory.getLogger(AtualizarFotoPerfilUsecaseImpl.class);

    private final FileStoragePort fileStoragePort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public String execute(MultipartFile file, String userId) {
        MDC.put("operation", "atualizarFotoPerfil");
        MDC.put("entityId", userId);
        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException("Arquivo é obrigatório", HttpStatus.BAD_REQUEST);
            }

            UserAccount account = userAccountRepositoryPort.findById(userId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));

            if (account.getFotoPerfil() != null) {
                try {
                    fileStoragePort.deletar(account.getFotoPerfil());
                } catch (IOException e) {
                    log.warn("Falha ao deletar foto antiga do perfil. userId={}", userId, e);
                }
            }

            String novaKey;
            try {
                novaKey = fileStoragePort.salvar(file);
            } catch (IOException e) {
                log.warn("Falha ao salvar foto de perfil. userId={}", userId, e);
                throw new BusinessException("Erro ao salvar arquivo", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            account.setFotoPerfil(novaKey);
            userAccountRepositoryPort.save(account);
            return novaKey;
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
```

- [ ] **Step 4: Implement `RemoverFotoPerfilUsecaseImpl`**

```java
// src/main/java/analu/whereio/application/service/usuario/RemoverFotoPerfilUsecaseImpl.java
package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.RemoverFotoPerfilUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RemoverFotoPerfilUsecaseImpl implements RemoverFotoPerfilUsecase {

    private static final Logger log = LoggerFactory.getLogger(RemoverFotoPerfilUsecaseImpl.class);

    private final FileStoragePort fileStoragePort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;

    @Override
    public void execute(String userId) {
        MDC.put("operation", "removerFotoPerfil");
        MDC.put("entityId", userId);
        try {
            UserAccount account = userAccountRepositoryPort.findById(userId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));

            if (account.getFotoPerfil() == null) {
                return;
            }

            try {
                fileStoragePort.deletar(account.getFotoPerfil());
            } catch (IOException e) {
                log.warn("Falha ao deletar foto de perfil. userId={}", userId, e);
            }

            account.setFotoPerfil(null);
            userAccountRepositoryPort.save(account);
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
```

- [ ] **Step 5: Run tests — verify PASS**

```bash
./mvnw test -Dtest="AtualizarFotoPerfilUsecaseImplTest,RemoverFotoPerfilUsecaseImplTest" -q
```

Expected: `BUILD SUCCESS`, 5 tests green.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/analu/whereio/application/service/usuario/AtualizarFotoPerfilUsecaseImpl.java
git add src/main/java/analu/whereio/application/service/usuario/RemoverFotoPerfilUsecaseImpl.java
git add src/test/java/analu/whereio/application/service/usuario/AtualizarFotoPerfilUsecaseImplTest.java
git add src/test/java/analu/whereio/application/service/usuario/RemoverFotoPerfilUsecaseImplTest.java
git commit -m "feat: AtualizarFotoPerfilUsecaseImpl and RemoverFotoPerfilUsecaseImpl"
```

---

## Task 9: UserConverter + UserController + UserControllerTest

**Files:**
- Create: `src/main/java/analu/whereio/adapters/in/web/converter/UserConverter.java`
- Create: `src/main/java/analu/whereio/adapters/in/web/UserController.java`
- Create: `src/test/java/analu/whereio/adapters/in/web/UserControllerTest.java`

- [ ] **Step 1: Create `UserConverter`**

```java
// src/main/java/analu/whereio/adapters/in/web/converter/UserConverter.java
package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.response.UsuarioBuscaResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioPerfilResponse;
import analu.whereio.application.model.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConverter {

    @Mapping(source = "nomeUsuario", target = "nomeUsuario")
    @Mapping(source = "nome", target = "nome")
    @Mapping(target = "fotoPerfilUrl", ignore = true)
    UsuarioBuscaResponse toBuscaResponse(UserAccount account);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "nomeUsuario", target = "nomeUsuario")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "fotoPerfilUrl", ignore = true)
    UsuarioPerfilResponse toPerfilResponse(UserAccount account);
}
```

- [ ] **Step 2: Write failing test for `UserController`**

```java
// src/test/java/analu/whereio/adapters/in/web/UserControllerTest.java
package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.UserConverter;
import analu.whereio.adapters.in.web.dto.request.AtualizarNomeRequest;
import analu.whereio.adapters.in.web.dto.response.FileUploadResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioBuscaResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioPerfilResponse;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.AtualizarFotoPerfilUsecase;
import analu.whereio.application.ports.in.usuario.AtualizarNomeUsuarioUsecase;
import analu.whereio.application.ports.in.usuario.BuscarUsuariosPorPrefixoUsecase;
import analu.whereio.application.ports.in.usuario.ObterPerfilUsuarioUsecase;
import analu.whereio.application.ports.in.usuario.RemoverFotoPerfilUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.config.security.JwtUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
class UserControllerTest {

    private static final JwtUserPrincipal PRINCIPAL = JwtUserPrincipal.testPrincipal("user-1");

    @Mock private BuscarUsuariosPorPrefixoUsecase buscarUsuariosUsecase;
    @Mock private ObterPerfilUsuarioUsecase obterPerfilUsecase;
    @Mock private AtualizarNomeUsuarioUsecase atualizarNomeUsecase;
    @Mock private AtualizarFotoPerfilUsecase atualizarFotoUsecase;
    @Mock private RemoverFotoPerfilUsecase removerFotoUsecase;
    @Mock private FileStoragePort fileStoragePort;
    @Mock private UserConverter converter;

    @InjectMocks
    private UserController userController;

    private UserAccount conta;

    @BeforeEach
    void setUp() {
        conta = new UserAccount();
        conta.setId("user-1");
        conta.setNomeUsuario("joao123");
        conta.setNome("João");
        conta.setEmail("joao@test.local");
        conta.setFotoPerfil(null);
    }

    @Nested
    @DisplayName("GET /api/usuario/buscar - buscar")
    class Buscar {

        @Test
        @DisplayName("deve retornar lista de até 10 usuários com fotoPerfilUrl preenchida")
        void deveRetornarListaComFotoPerfilUrl() {
            UserAccount comFoto = new UserAccount();
            comFoto.setId("user-2");
            comFoto.setNomeUsuario("joana");
            comFoto.setFotoPerfil("foto_key.jpg");

            UsuarioBuscaResponse resp1 = new UsuarioBuscaResponse();
            resp1.setNomeUsuario("joao123");

            UsuarioBuscaResponse resp2 = new UsuarioBuscaResponse();
            resp2.setNomeUsuario("joana");

            when(buscarUsuariosUsecase.execute("joa", "user-1")).thenReturn(List.of(conta, comFoto));
            when(converter.toBuscaResponse(conta)).thenReturn(resp1);
            when(converter.toBuscaResponse(comFoto)).thenReturn(resp2);
            when(fileStoragePort.gerarUrlAssinada("foto_key.jpg")).thenReturn("/media/foto_key.jpg");

            ResponseEntity<List<UsuarioBuscaResponse>> resposta = userController.buscar(PRINCIPAL, "joa");

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals(2, resposta.getBody().size()),
                    () -> assertNull(resposta.getBody().get(0).getFotoPerfilUrl()),
                    () -> assertEquals("/media/foto_key.jpg", resposta.getBody().get(1).getFotoPerfilUrl())
            );
        }
    }

    @Nested
    @DisplayName("GET /api/usuario/perfil - obterPerfil")
    class ObterPerfil {

        @Test
        @DisplayName("deve retornar UsuarioPerfilResponse com fotoPerfilUrl")
        void deveRetornarPerfilComFotoUrl() {
            conta.setFotoPerfil("minha_foto.jpg");
            UsuarioPerfilResponse perfilResp = new UsuarioPerfilResponse();
            perfilResp.setId("user-1");

            when(obterPerfilUsecase.execute("user-1")).thenReturn(conta);
            when(converter.toPerfilResponse(conta)).thenReturn(perfilResp);
            when(fileStoragePort.gerarUrlAssinada("minha_foto.jpg")).thenReturn("/media/minha_foto.jpg");

            ResponseEntity<UsuarioPerfilResponse> resposta = userController.obterPerfil(PRINCIPAL);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals("/media/minha_foto.jpg", resposta.getBody().getFotoPerfilUrl())
            );
        }
    }

    @Nested
    @DisplayName("PATCH /api/usuario/nome - atualizarNome")
    class AtualizarNome {

        @Test
        @DisplayName("deve atualizar nome e retornar 200 com UsuarioPerfilResponse")
        void deveAtualizarNomeERetornar200() {
            AtualizarNomeRequest req = new AtualizarNomeRequest();
            req.setNome("Novo Nome");

            UserAccount atualizado = new UserAccount();
            atualizado.setId("user-1");
            atualizado.setNome("Novo Nome");

            UsuarioPerfilResponse perfilResp = new UsuarioPerfilResponse();
            perfilResp.setNome("Novo Nome");

            when(atualizarNomeUsecase.execute("user-1", "Novo Nome")).thenReturn(atualizado);
            when(converter.toPerfilResponse(atualizado)).thenReturn(perfilResp);

            ResponseEntity<UsuarioPerfilResponse> resposta = userController.atualizarNome(PRINCIPAL, req);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals("Novo Nome", resposta.getBody().getNome())
            );
            verify(atualizarNomeUsecase).execute("user-1", "Novo Nome");
        }
    }

    @Nested
    @DisplayName("PUT /api/usuario/foto-perfil - atualizarFoto")
    class AtualizarFoto {

        @Test
        @DisplayName("deve fazer upload e retornar FileUploadResponse")
        void deveFazerUploadERetornar200() {
            MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[]{1, 2});

            when(atualizarFotoUsecase.execute(file, "user-1")).thenReturn("nova_key.jpg");
            when(fileStoragePort.gerarUrlAssinada("nova_key.jpg")).thenReturn("/media/nova_key.jpg");

            ResponseEntity<FileUploadResponse> resposta = userController.atualizarFoto(PRINCIPAL, file);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals("nova_key.jpg", resposta.getBody().getFileName()),
                    () -> assertEquals("/media/nova_key.jpg", resposta.getBody().getUrlPath())
            );
        }
    }

    @Nested
    @DisplayName("DELETE /api/usuario/foto-perfil - removerFoto")
    class RemoverFoto {

        @Test
        @DisplayName("deve remover foto e retornar 204")
        void deveRemoverFotoERetornar204() {
            ResponseEntity<Void> resposta = userController.removerFoto(PRINCIPAL);

            assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            verify(removerFotoUsecase).execute("user-1");
        }
    }
}
```

- [ ] **Step 3: Run test to verify FAIL**

```bash
./mvnw test -Dtest="UserControllerTest" -q 2>&1 | tail -5
```

Expected: `BUILD FAILURE`

- [ ] **Step 4: Implement `UserController`**

```java
// src/main/java/analu/whereio/adapters/in/web/UserController.java
package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.UserConverter;
import analu.whereio.adapters.in.web.dto.request.AtualizarNomeRequest;
import analu.whereio.adapters.in.web.dto.response.FileUploadResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioBuscaResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioPerfilResponse;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.AtualizarFotoPerfilUsecase;
import analu.whereio.application.ports.in.usuario.AtualizarNomeUsuarioUsecase;
import analu.whereio.application.ports.in.usuario.BuscarUsuariosPorPrefixoUsecase;
import analu.whereio.application.ports.in.usuario.ObterPerfilUsuarioUsecase;
import analu.whereio.application.ports.in.usuario.RemoverFotoPerfilUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.config.security.JwtUserPrincipal;
import analu.whereio.exceptions.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UserController {

    private final BuscarUsuariosPorPrefixoUsecase buscarUsuariosUsecase;
    private final ObterPerfilUsuarioUsecase obterPerfilUsecase;
    private final AtualizarNomeUsuarioUsecase atualizarNomeUsecase;
    private final AtualizarFotoPerfilUsecase atualizarFotoUsecase;
    private final RemoverFotoPerfilUsecase removerFotoUsecase;
    private final FileStoragePort fileStoragePort;
    private final UserConverter converter;

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioBuscaResponse>> buscar(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam String q) {
        List<UsuarioBuscaResponse> resultado = buscarUsuariosUsecase.execute(q, principal.getUserId())
                .stream()
                .map(account -> {
                    UsuarioBuscaResponse resp = converter.toBuscaResponse(account);
                    if (account.getFotoPerfil() != null) {
                        resp.setFotoPerfilUrl(fileStoragePort.gerarUrlAssinada(account.getFotoPerfil()));
                    }
                    return resp;
                })
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioPerfilResponse> obterPerfil(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        UserAccount account = obterPerfilUsecase.execute(principal.getUserId());
        UsuarioPerfilResponse resp = converter.toPerfilResponse(account);
        if (account.getFotoPerfil() != null) {
            resp.setFotoPerfilUrl(fileStoragePort.gerarUrlAssinada(account.getFotoPerfil()));
        }
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/nome")
    public ResponseEntity<UsuarioPerfilResponse> atualizarNome(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody AtualizarNomeRequest request) {
        UserAccount atualizado = atualizarNomeUsecase.execute(principal.getUserId(), request.getNome());
        UsuarioPerfilResponse resp = converter.toPerfilResponse(atualizado);
        if (atualizado.getFotoPerfil() != null) {
            resp.setFotoPerfilUrl(fileStoragePort.gerarUrlAssinada(atualizado.getFotoPerfil()));
        }
        return ResponseEntity.ok(resp);
    }

    @PutMapping(value = "/foto-perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> atualizarFoto(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo é obrigatório", HttpStatus.BAD_REQUEST);
        }
        String key = atualizarFotoUsecase.execute(file, principal.getUserId());
        String url = fileStoragePort.gerarUrlAssinada(key);
        return ResponseEntity.ok(new FileUploadResponse(key, url));
    }

    @DeleteMapping("/foto-perfil")
    public ResponseEntity<Void> removerFoto(@AuthenticationPrincipal JwtUserPrincipal principal) {
        removerFotoUsecase.execute(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 5: Run tests — verify PASS**

```bash
./mvnw test -Dtest="UserControllerTest" -q
```

Expected: `BUILD SUCCESS`, all tests green.

- [ ] **Step 6: Run full test suite**

```bash
./mvnw test -q
```

Expected: `BUILD SUCCESS`, all tests green.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/analu/whereio/adapters/in/web/converter/UserConverter.java
git add src/main/java/analu/whereio/adapters/in/web/UserController.java
git add src/test/java/analu/whereio/adapters/in/web/UserControllerTest.java
git commit -m "feat: UserController with buscar, perfil, nome, foto-perfil endpoints"
```

---

## New Endpoints Summary (for mobile client)

| Método | Path | Body / Param | Response | Auth |
|--------|------|-------------|----------|------|
| `POST` | `/api/auth/register` | `{ email, password, nome?, nomeUsuario* }` — `nomeUsuario` agora obrigatório `[a-z0-9_]{3,20}` | `{ accessToken, refreshToken, ... }` | ❌ |
| `GET` | `/api/usuario/buscar?q={prefix}` | query param `q` (min 1 char, lowercase) | `[{ nomeUsuario, nome, fotoPerfilUrl }]` (máx 10) | ✅ |
| `GET` | `/api/usuario/perfil` | — | `{ id, nome, nomeUsuario, email, fotoPerfilUrl }` | ✅ |
| `PATCH` | `/api/usuario/nome` | `{ "nome": "Novo Nome" }` | `{ id, nome, nomeUsuario, email, fotoPerfilUrl }` | ✅ |
| `PUT` | `/api/usuario/foto-perfil` | `multipart/form-data`, campo `file` | `{ fileName, urlPath }` | ✅ |
| `DELETE` | `/api/usuario/foto-perfil` | — | `204 No Content` | ✅ |

**Endpoint alterado:**
- `GET /api/amigos` — `AmigoPerfilResponse` agora tem `fotoPerfilUrl` em vez de `email`
