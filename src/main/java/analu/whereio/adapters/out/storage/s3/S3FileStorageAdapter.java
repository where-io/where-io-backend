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
import java.io.InputStream;
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

    @Override
    public String gerarUrlAssinada(String key) {
        return s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(props.getPresignDurationMinutes()))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(props.getBucket())
                                .key(key)
                                .build())
                        .build()
        ).url().toString();
    }

    @Override
    public InputStream getObject(String key) {
        return s3Client.getObject(GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build());
    }
}
