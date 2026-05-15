package analu.whereio.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class UploadedFilesWebConfig implements WebMvcConfigurer {

    private final FileStorageConfig fileStorageConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Path.of(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
        String location = dir.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/media/**")
                .addResourceLocations(location);
    }
}
