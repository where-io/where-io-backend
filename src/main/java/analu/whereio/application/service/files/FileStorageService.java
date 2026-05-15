package analu.whereio.application.service.files;

import analu.whereio.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig config;

    public String saveFile(MultipartFile file) throws IOException {

        String fileName =
                UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path uploadPath = Paths.get(config.getUploadDir());

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return fileName;
    }

    /**
     * Apaga arquivo dentro exclusivamente do diretório de upload (bloqueia path traversal).
     */
    public void deleteStoredFile(String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName");
        }
        String trimmed = fileName.trim();
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
}
