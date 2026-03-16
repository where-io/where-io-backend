package analu.whereio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClienteConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://places.googleapis.com") // base fixa para todas as chamadas
                .build();
    }
}
