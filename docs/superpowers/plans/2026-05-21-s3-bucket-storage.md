# S3 Bucket Storage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace local disk file storage with Railway S3-compatible Bucket, keeping local disk as fallback for development via `STORAGE_TYPE` env var.

**Architecture:** Define `FileStoragePort` output port interface; create `LocalFileStorageAdapter` (disk, default) and `S3FileStorageAdapter` (Railway Bucket, activated by `STORAGE_TYPE=s3`) implementing it. All use cases and controllers depend only on the port interface. URL generation is delegated to the port: local returns `/media/{key}`, S3 returns a pre-signed URL.

**Tech Stack:** Spring Boot 4 / Java 17, AWS SDK v2 (`software.amazon.awssdk:s3:2.26.12`), MapStruct 1.5.5, `@ConditionalOnProperty` for adapter activation.

---

## File Map

| Action | Path |
|--------|------|
| CREATE | `application/ports/out/FileStoragePort.java` |
| CREATE | `adapters/out/storage/local/LocalFileStorageAdapter.java` |
| CREATE | `adapters/out/storage/s3/S3FileStorageAdapter.java` |
| CREATE | `config/S3StorageProperties.java` |
| CREATE | `config/S3Config.java` |
| MODIFY | `pom.xml` — add AWS SDK v2 S3 dependency |
| MODIFY | `application.yml` — add `storage.*` properties |
| MODIFY | `application/service/local/AdicionarFotoLocalUsecaseImpl.java` — swap `FileStorageService` → `FileStoragePort` |
| MODIFY | `application/service/local/RemoverFotoLocalUsecaseImpl.java` — swap `FileStorageService` → `FileStoragePort` |
| MODIFY | `adapters/in/web/FileController.java` — swap `FileStorageService` → `FileStoragePort`, URL via port |
| MODIFY | `adapters/in/web/converter/LocalConverter.java` — interface → abstract class, inject `FileStoragePort` |
| DELETE | `application/service/files/FileStorageService.java` |
| MODIFY | `test/.../AdicionarFotoLocalUsecaseImplTest.java` — mock `FileStoragePort` instead of `FileStorageService` |
| MODIFY | `test/.../RemoverFotoLocalUsecaseImplTest.java` — mock `FileStoragePort` instead of `FileStorageService` |

---

## Task 1: Add AWS SDK v2 dependency

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add dependency inside `<dependencies>`**

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.26.12</version>
</dependency>
```

- [ ] **Step 2: Verify compile**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: add AWS SDK v2 S3 dependency"
```

---

## Task 2: Create `FileStoragePort` output port

**Files:**
- Create: `src/main/java/analu/whereio/application/ports/out/FileStoragePort.java`

- [ ] **Step 1: Create interface**

```java
package analu.whereio.application.ports.out;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStoragePort {

    /**
     * Persiste o arquivo e retorna a key (ex: uuid_foto.jpg).
     */
    String salvar(MultipartFile file) throws IOException;

    /**
     * Remove o arquivo pelo key. Implementações devem tolerar key inexistente.
     */
    void deletar(String key) throws IOException;

    /**
     * Retorna URL de acesso ao arquivo.
     * Local: /media/{key}  |  S3: URL pre-signed com expiração configurável.
     */
    String gerarUrlAssinada(String key);
}
```

- [ ] **Step 2: Compile**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

---

## Task 3: Create `LocalFileStorageAdapter`

**Files:**
- Create: `src/main/java/analu/whereio/adapters/out/storage/local/LocalFileStorageAdapter.java`

- [ ] **Step 1: Create adapter**

```java
package analu.whereio.adapters.out.storage.local;

import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalFileStorageAdapter implements FileStoragePort {

    private final FileStorageConfig config;

    @Override
    public String salvar(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(config.getUploadDir());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    @Override
    public void deletar(String key) throws IOException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("fileName");
        }
        String trimmed = key.trim();
        if (trimmed.contains("..") || trimmed.contains("/") || trimmed.contains("\\")) {
            throw new IllegalArgumentException("invalid fileName");
        }
        Path base = Paths.get(config.getUploadDir()).normalize().toAbsolutePath();
        Path target = base.resolve(trimmed).normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("path outside upload dir");
        }
        Files.deleteIfExists(target);
    }

    @Override
    public String gerarUrlAssinada(String key) {
        return "/media/" + key;
    }
}
```

- [ ] **Step 2: Compile**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

---

## Task 4: Create `S3StorageProperties` and `S3Config`

**Files:**
- Create: `src/main/java/analu/whereio/config/S3StorageProperties.java`
- Create: `src/main/java/analu/whereio/config/S3Config.java`

- [ ] **Step 1: Create `S3StorageProperties`**

```java
package analu.whereio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "storage.s3")
@Getter
@Setter
public class S3StorageProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region = "us-east-1";
    private int presignDurationMinutes = 60;
}
```

- [ ] **Step 2: Create `S3Config`**

```java
package analu.whereio.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3Config {

    @Bean
    public S3Client s3Client(S3StorageProperties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
                ))
                .region(Region.of(props.getRegion()))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3StorageProperties props) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
                ))
                .region(Region.of(props.getRegion()))
                .build();
    }
}
```

- [ ] **Step 3: Compile**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

---

## Task 5: Create `S3FileStorageAdapter`

**Files:**
- Create: `src/main/java/analu/whereio/adapters/out/storage/s3/S3FileStorageAdapter.java`

- [ ] **Step 1: Create adapter**

```java
package analu.whereio.adapters.out.storage.s3;

import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.config.S3StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3StorageProperties props;

    @Override
    public String salvar(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(props.getBucket())
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
        return key;
    }

    @Override
    public void deletar(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(props.getBucket())
                        .key(key)
                        .build()
        );
    }

    @Override
    public String gerarUrlAssinada(String key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(props.getPresignDurationMinutes()))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(props.getBucket())
                        .key(key)
                        .build())
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
```

- [ ] **Step 2: Compile**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

---

## Task 6: Update `application.yml`

**Files:**
- Modify: `src/main/resources/application.yml`

- [ ] **Step 1: Add storage config block below `file:` block**

```yaml
storage:
  type: ${STORAGE_TYPE:local}
  s3:
    endpoint: ${AWS_S3_ENDPOINT:}
    access-key: ${AWS_S3_ACCESS_KEY:}
    secret-key: ${AWS_S3_SECRET_KEY:}
    bucket: ${AWS_S3_BUCKET:}
    region: ${AWS_S3_REGION:us-east-1}
    presign-duration-minutes: ${AWS_S3_PRESIGN_MINUTES:60}
```

---

## Task 7: Update use cases to use `FileStoragePort`

**Files:**
- Modify: `src/main/java/analu/whereio/application/service/local/AdicionarFotoLocalUsecaseImpl.java`
- Modify: `src/main/java/analu/whereio/application/service/local/RemoverFotoLocalUsecaseImpl.java`

- [ ] **Step 1: Update `AdicionarFotoLocalUsecaseImpl`**

Replace import and field:
- Remove: `import analu.whereio.application.service.files.FileStorageService;`
- Add: `import analu.whereio.application.ports.out.FileStoragePort;`
- Change field: `private final FileStorageService fileStorageService;` → `private final FileStoragePort fileStoragePort;`
- Change call: `fileStorageService.saveFile(file)` → `fileStoragePort.salvar(file)`

- [ ] **Step 2: Update `RemoverFotoLocalUsecaseImpl`**

Replace import and field:
- Remove: `import analu.whereio.application.service.files.FileStorageService;`
- Add: `import analu.whereio.application.ports.out.FileStoragePort;`
- Change field: `private final FileStorageService fileStorageService;` → `private final FileStoragePort fileStoragePort;`
- Change call: `fileStorageService.deleteStoredFile(fileName)` → `fileStoragePort.deletar(fileName)`

- [ ] **Step 3: Compile**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

---

## Task 8: Update `FileController`

**Files:**
- Modify: `src/main/java/analu/whereio/adapters/in/web/FileController.java`

- [ ] **Step 1: Replace `FileStorageService` with `FileStoragePort`**

- Remove: `import analu.whereio.application.service.files.FileStorageService;`
- Add: `import analu.whereio.application.ports.out.FileStoragePort;`
- Change field: `private final FileStorageService fileStorageService;` → `private final FileStoragePort fileStoragePort;`
- In `upload()`: `fileStorageService.saveFile(file)` → `fileStoragePort.salvar(file)`
- In `upload()`: `String urlPath = "/media/" + storedName;` → `String urlPath = fileStoragePort.gerarUrlAssinada(storedName);`
- In `listarFotosDoLocal()`: `.map(n -> new FileUploadResponse(n, "/media/" + n))` → `.map(n -> new FileUploadResponse(n, fileStoragePort.gerarUrlAssinada(n)))`

- [ ] **Step 2: Compile**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

---

## Task 9: Convert `LocalConverter` to abstract class

**Files:**
- Modify: `src/main/java/analu/whereio/adapters/in/web/converter/LocalConverter.java`

- [ ] **Step 1: Rewrite as abstract class**

```java
package analu.whereio.adapters.in.web.converter;

import analu.whereio.adapters.in.web.dto.request.LocalDtoRequest;
import analu.whereio.adapters.in.web.dto.response.LocalBuscarDtoResponse;
import analu.whereio.adapters.in.web.dto.response.LocalDtoResponse;
import analu.whereio.adapters.in.web.dto.response.PlaceDetailsDtoResponse;
import analu.whereio.adapters.in.web.dto.response.PredictionDto;
import analu.whereio.adapters.in.web.dto.response.StructuredFormattingDto;
import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.adapters.out.external.geocoding.record.PlaceDetailsRecord;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.out.FileStoragePort;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = VisitaConverter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LocalConverter {

    @Autowired
    protected FileStoragePort fileStoragePort;

    public abstract Local toDomain(LocalDtoRequest localDtoRequest);

    @Mapping(target = "fotoUrls", source = "fotos", qualifiedByName = "localFotosToUrls")
    public abstract LocalDtoResponse toResponse(Local local);

    @Named("localFotosToUrls")
    List<String> localFotosToUrls(List<String> fotos) {
        if (fotos == null || fotos.isEmpty()) {
            return List.of();
        }
        return fotos.stream().map(fileStoragePort::gerarUrlAssinada).toList();
    }

    public LocalBuscarDtoResponse toBuscarResponse(AutoCompleteResponse autoCompleteResponse) {
        LocalBuscarDtoResponse out = new LocalBuscarDtoResponse();
        if (autoCompleteResponse == null || autoCompleteResponse.suggestions() == null) {
            out.setPredictions(List.of());
            return out;
        }
        out.setPredictions(autoCompleteResponse.suggestions().stream().map(s -> {
            PredictionDto p = new PredictionDto();
            p.setPlaceId(s.placeId());
            p.setDescription(s.description());
            StructuredFormattingDto sf = new StructuredFormattingDto();
            sf.setMainText(s.mainText());
            sf.setSecondaryText(s.secondaryText());
            p.setStructuredFormatting(sf);
            return p;
        }).toList());
        return out;
    }

    public PlaceDetailsDtoResponse toPlaceDetailsResponse(PlaceDetailsRecord r) {
        PlaceDetailsDtoResponse o = new PlaceDetailsDtoResponse();
        o.setLat(r.lat());
        o.setLng(r.lng());
        o.setLogradouro(r.logradouro());
        o.setBairro(r.bairro());
        o.setCidade(r.cidade());
        o.setEstado(r.estado());
        o.setCep(r.cep());
        o.setPais(r.pais());
        o.setFormattedAddress(r.formattedAddress());
        return o;
    }
}
```

- [ ] **Step 2: Compile**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

---

## Task 10: Delete `FileStorageService`

**Files:**
- Delete: `src/main/java/analu/whereio/application/service/files/FileStorageService.java`

- [ ] **Step 1: Delete file**

```bash
rm src/main/java/analu/whereio/application/service/files/FileStorageService.java
```

- [ ] **Step 2: Compile**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS (no remaining references)

---

## Task 11: Update tests

**Files:**
- Modify: `src/test/java/analu/whereio/application/service/local/AdicionarFotoLocalUsecaseImplTest.java`
- Modify: `src/test/java/analu/whereio/application/service/local/RemoverFotoLocalUsecaseImplTest.java`

- [ ] **Step 1: Update `AdicionarFotoLocalUsecaseImplTest`**

- Remove: `import analu.whereio.application.service.files.FileStorageService;`
- Add: `import analu.whereio.application.ports.out.FileStoragePort;`
- Change `@Mock FileStorageService fileStorageService;` → `@Mock FileStoragePort fileStoragePort;`
- Change all `fileStorageService.saveFile(any())` → `fileStoragePort.salvar(any())`
- Change `verify(fileStorageService, never()).saveFile(any())` → `verify(fileStoragePort, never()).salvar(any())`
- In `fluxoCompleto()`: `when(fileStorageService.saveFile(any())).thenReturn("uuid_foto.jpg")` → `when(fileStoragePort.salvar(any())).thenReturn("uuid_foto.jpg")`
- In `listaExistente()`: same swap

- [ ] **Step 2: Update `RemoverFotoLocalUsecaseImplTest`**

- Remove: `import analu.whereio.application.service.files.FileStorageService;`
- Add: `import analu.whereio.application.ports.out.FileStoragePort;`
- Change `@Mock FileStorageService fileStorageService;` → `@Mock FileStoragePort fileStoragePort;`
- Change `fileStorageService.deleteStoredFile(any())` → `fileStoragePort.deletar(any())`
- Change `verify(fileStorageService, never()).deleteStoredFile(any())` → `verify(fileStoragePort, never()).deletar(any())`
- In `removeListaEPersisteEApagaArquivo()`: `doNothing().when(fileStorageService).deleteStoredFile(FILE)` → `doNothing().when(fileStoragePort).deletar(FILE)`

- [ ] **Step 3: Run tests**

```bash
./mvnw test -Dtest=AdicionarFotoLocalUsecaseImplTest,RemoverFotoLocalUsecaseImplTest
```
Expected: BUILD SUCCESS, all tests GREEN

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: migrate file storage to S3-compatible Railway Bucket

- Add FileStoragePort output port interface
- LocalFileStorageAdapter (disk, default STORAGE_TYPE=local)
- S3FileStorageAdapter (Railway Bucket, STORAGE_TYPE=s3)
- S3Config + S3StorageProperties for AWS SDK v2 beans
- Update use cases, FileController, LocalConverter
- Delete FileStorageService (logic moved to adapters)
- Update tests to mock FileStoragePort"
```
