# LocalFotoController — Design Spec

**Date:** 2026-05-23  
**Status:** Approved

## Problem

`FileController` misturava proxy de arquivos com CRUD de fotos. Com S3 + presigned URLs o proxy virou dead code. Imagens não carregavam no mobile porque o proxy (`GET /api/files/{key}`) tentava fazer `S3Client.getObject()` sem garantia de permissão `s3:GetObject`.

## Goal

3 endpoints REST limpos para fotos vinculadas a local + usuário. Imagens carregam direto do S3 via presigned URL — sem proxy no backend.

---

## API Design

Base: `/api/local/{idLocal}/fotos`

### POST `/api/local/{idLocal}/fotos`
- **Auth**: JWT obrigatório
- **Body**: `multipart/form-data` com campo `file`
- **Logic**: valida dono do local, salva no S3, associa fileName ao `local.fotos`
- **Response 200**: `{ fileName, urlPath }` onde `urlPath` é presigned URL do S3

### GET `/api/local/{idLocal}/fotos`
- **Auth**: JWT obrigatório
- **Logic**: valida dono do local, retorna lista de fotos com presigned URLs frescas
- **Response 200**: `List<{ fileName, urlPath }>`

### DELETE `/api/local/{idLocal}/fotos/{fileName}`
- **Auth**: JWT obrigatório
- **Path var**: `{fileName:.+}` — captura UUID + extensão (ex: `uuid_img.jpg`)
- **Logic**: valida dono, remove do S3, remove de `local.fotos`
- **Response 200**: `List<{ fileName, urlPath }>` — lista atualizada após remoção

---

## Changes

### Backend

| Arquivo | Ação |
|---------|------|
| `FileController.java` | Deletado |
| `LocalFotoController.java` | Criado em `adapters/in/web/` |
| `SecurityConfig.java` | Remove `permitAll` para `/api/files/**` e `/files/**` |

Use cases reutilizados sem alteração:
- `AdicionarFotoLocalUsecase`
- `ListarFotosPorIdLocalUsecase`
- `RemoverFotoLocalUsecase`

### Mobile (`LocaisService.ts`)

| Função | Antes | Depois |
|--------|-------|--------|
| `uploadFile` | `POST /api/files/upload` com `idLocal` query param | `POST /api/local/${idLocal}/fotos` |
| `listLocalFotos` | `GET /api/files/local/${id}/fotos` | `GET /api/local/${id}/fotos` |
| `deleteLocalFoto` | `DELETE /api/files/local/${id}/fotos?fileName=X` → `boolean` | `DELETE /api/local/${id}/fotos/${encodedFileName}` → `FotoResponse[]` |

`deleteLocalFoto` retorna lista atualizada → carousel usa diretamente sem segundo fetch.

---

## Security

- Todos os 3 endpoints exigem JWT
- Propriedade do local validada via `ownerUserId` (use cases existentes)
- Imagens no S3 carregadas via presigned URL (60 min expiry) — mobile não passa JWT pro S3

---

## Out of Scope

- Proxy endpoint (`GET /api/files/{key}`) — removido, não necessário com presigned URLs
- Upload sem `idLocal` — removido (era feature desnecessária)
- Adapter local (`LocalFileStorageAdapter`) — não muda
