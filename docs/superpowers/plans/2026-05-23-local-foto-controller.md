# LocalFotoController Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Substituir FileController por LocalFotoController com 3 endpoints REST limpos para fotos vinculadas a local + usuário, carregando imagens direto do S3 via presigned URLs.

**Architecture:** `LocalFotoController` assume o path `/api/local/{idLocal}/fotos` e reutiliza os 3 use cases existentes. O proxy de arquivo (`GET /api/files/{key}`) é removido — imagens carregam via presigned URL gerada pelo `S3FileStorageAdapter`. Mobile atualiza URLs e usa lista retornada pelo DELETE diretamente.

**Tech Stack:** Spring Boot, Java 17, AWS SDK v2 S3Presigner, React Native (Expo), TypeScript

---

## File Map

| Arquivo | Ação |
|---------|------|
| `backend/src/main/java/analu/whereio/adapters/in/web/FileController.java` | Deletar |
| `backend/src/main/java/analu/whereio/adapters/in/web/LocalFotoController.java` | Criar |
| `backend/src/main/java/analu/whereio/config/security/SecurityConfig.java` | Modificar — remover permitAll `/api/files/**` |
| `mobile/src/service/LocaisService.ts` | Modificar — 3 funções |
| `mobile/src/components/LocalPhotosCarousel.tsx` | Modificar — `confirmDelete` |

---

### Task 1: Criar LocalFotoController

**Files:**
- Create: `backend/src/main/java/analu/whereio/adapters/in/web/LocalFotoController.java`

- [ ] **Step 1: Criar o arquivo**

```java
package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.dto.response.FileUploadResponse;
import analu.whereio.application.ports.in.local.AdicionarFotoLocalUsecase;
import analu.whereio.application.ports.in.local.ListarFotosPorIdLocalUsecase;
import analu.whereio.application.ports.in.local.RemoverFotoLocalUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.config.security.JwtUserPrincipal;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/local")
@RequiredArgsConstructor
public class LocalFotoController {

    private final FileStoragePort fileStoragePort;
    private final AdicionarFotoLocalUsecase adicionarFotoLocalUsecase;
    private final ListarFotosPorIdLocalUsecase listarFotosPorIdLocalUsecase;
    private final RemoverFotoLocalUsecase removerFotoLocalUsecase;

    @PostMapping(value = "/{idLocal}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal,
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo é obrigatório", HttpStatus.BAD_REQUEST);
        }
        String storedName = adicionarFotoLocalUsecase.execute(file, idLocal, principal.getUserId());
        String urlPath = fileStoragePort.gerarUrlAssinada(storedName);
        return ResponseEntity.ok(new FileUploadResponse(storedName, urlPath));
    }

    @GetMapping("/{idLocal}/fotos")
    public ResponseEntity<List<FileUploadResponse>> listar(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal
    ) {
        List<String> nomes = listarFotosPorIdLocalUsecase.execute(idLocal, principal.getUserId());
        List<FileUploadResponse> body = nomes.stream()
                .map(n -> new FileUploadResponse(n, fileStoragePort.gerarUrlAssinada(n)))
                .toList();
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{idLocal}/fotos/{fileName:.+}")
    public ResponseEntity<List<FileUploadResponse>> remover(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable String idLocal,
            @PathVariable String fileName
    ) {
        removerFotoLocalUsecase.execute(idLocal, fileName, principal.getUserId());
        List<String> remaining = listarFotosPorIdLocalUsecase.execute(idLocal, principal.getUserId());
        List<FileUploadResponse> body = remaining.stream()
                .map(n -> new FileUploadResponse(n, fileStoragePort.gerarUrlAssinada(n)))
                .toList();
        return ResponseEntity.ok(body);
    }
}
```

- [ ] **Step 2: Compilar para verificar**

```bash
cd backend && ./mvnw compile -q
```

Expected: BUILD SUCCESS sem erros.

---

### Task 2: Deletar FileController e limpar SecurityConfig

**Files:**
- Delete: `backend/src/main/java/analu/whereio/adapters/in/web/FileController.java`
- Modify: `backend/src/main/java/analu/whereio/config/security/SecurityConfig.java`

- [ ] **Step 1: Deletar FileController.java**

```bash
rm backend/src/main/java/analu/whereio/adapters/in/web/FileController.java
```

- [ ] **Step 2: Remover permitAll obsoleto no SecurityConfig**

Em `SecurityConfig.java`, remover a linha:
```java
.requestMatchers(HttpMethod.GET, "/api/files/**", "/files/**").permitAll()
```

O bloco fica assim após remoção:
```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/media/**").permitAll()
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/internal/**").permitAll()
        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
        .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html")
        .permitAll()
        .anyRequest().authenticated())
```

- [ ] **Step 3: Compilar**

```bash
cd backend && ./mvnw compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit backend**

```bash
git add backend/src/main/java/analu/whereio/adapters/in/web/LocalFotoController.java \
        backend/src/main/java/analu/whereio/config/security/SecurityConfig.java \
        backend/src/main/java/analu/whereio/adapters/in/web/FileController.java
git commit -m "refactor: replace FileController with LocalFotoController

3 REST endpoints under /api/local/{idLocal}/fotos:
- POST  → upload photo linked to local+user
- GET   → list photos (presigned S3 URLs)
- DELETE → remove photo, return updated list (200)

Remove /api/files proxy — images load directly from S3 via presigned URL.
Remove permitAll for /api/files/** (no longer needed)."
```

---

### Task 3: Atualizar mobile — LocaisService.ts

**Files:**
- Modify: `mobile/src/service/LocaisService.ts`

- [ ] **Step 1: Atualizar `uploadFile`**

Substituir a função `uploadFile` completa (linhas 143–175 do arquivo atual):

```typescript
export async function uploadFile(
  imageUri: string,
  idLocal: string,
  meta?: UploadImageMeta,
): Promise<FotoResponse | null> {
  const formData = new FormData();

  const rawName =
    (meta?.fileName && String(meta.fileName).trim()) ||
    imageUri.split('/').pop()?.split('?')[0] ||
    'photo.jpg';
  const filename = rawName.includes('.') ? rawName : `${rawName}.jpg`;
  const ext = filename.split('.').pop()?.toLowerCase() ?? 'jpg';
  const type =
    (meta?.mimeType && String(meta.mimeType).trim()) ||
    (ext === 'png' ? 'image/png' : ext === 'webp' ? 'image/webp' : 'image/jpeg');

  if (Platform.OS === 'web') {
    const blobRes = await fetch(imageUri);
    const blob = await blobRes.blob();
    const mime = blob.type && blob.type !== 'application/octet-stream' ? blob.type : type;
    const file = new File([blob], filename, { type: mime });
    formData.append('file', file);
  } else {
    formData.append('file', { uri: imageUri, name: filename, type } as any);
  }

  const res = await apiFetch(
    `/api/local/${encodeURIComponent(idLocal)}/fotos`,
    { method: 'POST', body: formData as any },
  );
  if (!res.ok) return null;
  return res.json();
}
```

- [ ] **Step 2: Atualizar `listLocalFotos`**

Substituir:
```typescript
export async function listLocalFotos(localId: string): Promise<FotoResponse[]> {
  const res = await apiFetch(`/api/local/${encodeURIComponent(localId)}/fotos`);
  if (!res.ok) return [];
  return res.json();
}
```

- [ ] **Step 3: Atualizar `deleteLocalFoto`**

Substituir (retorna lista atualizada ou null):
```typescript
export async function deleteLocalFoto(
  localId: string,
  fileName: string,
): Promise<FotoResponse[] | null> {
  const res = await apiFetch(
    `/api/local/${encodeURIComponent(localId)}/fotos/${encodeURIComponent(fileName)}`,
    { method: 'DELETE' },
  );
  if (!res.ok) return null;
  return res.json();
}
```

---

### Task 4: Atualizar mobile — LocalPhotosCarousel.tsx

**Files:**
- Modify: `mobile/src/components/LocalPhotosCarousel.tsx`

- [ ] **Step 1: Atualizar `confirmDelete` para usar lista retornada**

Substituir a função `confirmDelete` (linhas 182–200 do arquivo atual):

```typescript
async function confirmDelete() {
  if (!current?.fileName || deleteBusy) return;
  setDeleteBusy(true);
  setError(null);
  try {
    const updated = await deleteLocalFoto(localId, current.fileName);
    if (!updated) {
      setError('Não foi possível remover a foto.');
      return;
    }
    setConfirmDeleteOpen(false);
    setItems(updated);
    setIndex(0);
    onPhotosChanged?.();
  } catch {
    setError('Não foi possível remover a foto.');
  } finally {
    setDeleteBusy(false);
  }
}
```

- [ ] **Step 2: Commit mobile**

```bash
git add mobile/src/service/LocaisService.ts \
        mobile/src/components/LocalPhotosCarousel.tsx
git commit -m "feat(mobile): update photo endpoints to /api/local/{id}/fotos

- uploadFile: POST /api/local/{id}/fotos (idLocal now required)
- listLocalFotos: GET /api/local/{id}/fotos
- deleteLocalFoto: DELETE /api/local/{id}/fotos/{fileName}, returns FotoResponse[]
- confirmDelete uses returned list directly (no extra fetch)"
```

---

### Task 5: Abrir Pull Request

- [ ] **Step 1: Push e abrir PR**

```bash
git push origin HEAD
gh pr create \
  --title "refactor: LocalFotoController + mobile photo endpoints" \
  --body "$(cat <<'EOF'
## Summary
- Remove `FileController` e proxy `/api/files/{key}` — não necessário com presigned URLs S3
- Novo `LocalFotoController` em `/api/local/{idLocal}/fotos` com 3 endpoints (POST/GET/DELETE)
- DELETE retorna 200 com lista atualizada de fotos
- Mobile atualiza URLs e usa lista do DELETE diretamente (sem fetch extra)
- Fix carregamento de imagens: mobile agora recebe presigned URLs absolutas do S3

## Test plan
- [ ] Upload foto em um local → imagem aparece no carousel
- [ ] Listar fotos → carousel carrega com imagens visíveis
- [ ] Deletar foto → carousel atualiza sem imagem deletada
- [ ] Verificar que `/api/files/**` retorna 404 (removido)

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```
