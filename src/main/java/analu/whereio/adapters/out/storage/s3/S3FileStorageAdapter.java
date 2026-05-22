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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final S3StorageProperties props;

    @Override
    public String salvar(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) contentType = "image/jpeg";

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(props.getBucket())
                        .key(key)
                        .contentType(contentType)
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

    /**
     * Retorna a URL pública do objeto: {endpoint}/{bucket}/{key}.
     * Requer que o bucket tenha política de leitura pública.
     */
    @Override
    public String gerarUrlAssinada(String key) {
        String base = props.getEndpoint().replaceAll("/+$", "");
        return base + "/" + props.getBucket() + "/" + key;
    }
}
