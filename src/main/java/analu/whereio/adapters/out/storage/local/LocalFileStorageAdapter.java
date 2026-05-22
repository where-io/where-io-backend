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
