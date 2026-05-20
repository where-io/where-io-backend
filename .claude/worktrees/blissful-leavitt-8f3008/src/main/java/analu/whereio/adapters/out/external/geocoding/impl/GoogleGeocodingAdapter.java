package analu.whereio.adapters.out.external.geocoding.impl;

import analu.whereio.adapters.out.external.geocoding.record.ApiResponse;
import analu.whereio.adapters.out.external.geocoding.record.LatitudeLongitudeRecord;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GoogleGeocodingAdapter implements LatitudeLongitudeInterfacePort{

    private static final Logger log = LoggerFactory.getLogger(GoogleGeocodingAdapter.class);

    private final RestTemplate restTemplate;

    private final WebClient webClient;

    @Value("${google.map.api.key}")
    private String apiKey;

    public LatitudeLongitudeRecord ConverterEnderecoParaCoordenadas(String endereco) throws IOException, InterruptedException {

        String uri = "https://maps.googleapis.com/maps/api/geocode/json";

        String finalUri = UriComponentsBuilder
                .fromUriString(uri)
                .queryParam("address", endereco)
                .queryParam("key", apiKey)
                .toUriString();

        try {
            log.info("Chamando Google Geocoding API. endereco={}", endereco);

            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(finalUri, JsonNode.class);

            JsonNode body = response.getBody();

            log.info("Resposta recebida do Google Geocoding API. resultados={}", body.path("results").size());

            String jsonReponse = body
                    .path("results")
                    .get(0)
                    .path("formatted_address")
                    .asText();

            System.out.println(jsonReponse);

             String latitude = body
                    .path("results")
                    .get(0)
                    .path("geometry")
                    .path("location")
                    .path("lat")
                    .asText();

            String longitude = body
                    .path("results")
                    .get(0)
                    .path("geometry")
                    .path("location")
                    .path("lng")
                    .asText();

            return new LatitudeLongitudeRecord(longitude, longitude);

        } catch (Exception e) {
            log.error("Falha na chamada ao Google Geocoding API. endereco={} erro={}", endereco, e.getMessage(), e);
            throw e;
        }
    }

    public ApiResponse buscarLocalizacao(String endereco) throws IOException, InterruptedException {
//        AutoCompleteRequest requestBody = new AutoCompleteRequest(
//                inputText,
//                List.of("br"),
//                sessionToken
//        );

        try {
            log.info("Chamando Google Geocoding API. endereco={}", endereco);

            ApiResponse location =  webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api/geocode/json")
                            .queryParam("key", apiKey)
                            .queryParam("address", endereco)
                            .build())
//                .header("X-Goog-Api-Key")
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            log.info("Resposta recebida do Google Geocoding API. resultados={}", location != null && location.getResults() != null ? location.getResults().size() : 0);

            return location;

        } catch (Exception e) {
            log.error("Falha na chamada ao Google Geocoding API. endereco={} erro={}", endereco, e.getMessage(), e);
            throw e;
        }
    }
}
