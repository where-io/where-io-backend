package analu.whereio.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3Config {

    private void validateProps(S3StorageProperties props) {
        if (props.getEndpoint() == null || props.getEndpoint().isBlank()) {
            throw new IllegalStateException(
                "storage.s3.endpoint (AWS_ENDPOINT_URL) is required when STORAGE_TYPE=s3");
        }
        if (props.getAccessKey() == null || props.getAccessKey().isBlank()) {
            throw new IllegalStateException(
                "storage.s3.access-key (AWS_ACCESS_KEY_ID) is required when STORAGE_TYPE=s3");
        }
        if (props.getSecretKey() == null || props.getSecretKey().isBlank()) {
            throw new IllegalStateException(
                "storage.s3.secret-key (AWS_SECRET_ACCESS_KEY) is required when STORAGE_TYPE=s3");
        }
        if (props.getBucket() == null || props.getBucket().isBlank()) {
            throw new IllegalStateException(
                "storage.s3.bucket (AWS_S3_BUCKET_NAME) is required when STORAGE_TYPE=s3");
        }
    }

    @Bean
    public S3Client s3Client(S3StorageProperties props) {
        validateProps(props);
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

}
