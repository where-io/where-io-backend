package analu.whereio.adapters.out.external.geocoding.impl;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteRequest;
import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.adapters.out.external.geocoding.record.LatitudeLongitudeRecord;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.cfgxml.internal.ConfigLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class GoogleGeocodingAdapter implements LatitudeLongitudeInterfacePort{

    private final RestTemplate restTemplate;

    private final WebClient webClient;

    @Value("${google.map.api.key}")
    private String apiKey;

    public LatitudeLongitudeRecord ConverterEnderecoParaCoordenadas(String endereco) throws IOException, InterruptedException {

        Properties props = new Properties();

        InputStream input = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("application.properties");

        props.load(input);

        String apikey = props.getProperty("google.map.api.key");

        String uri = "https://maps.googleapis.com/maps/api/geocode/json";

        String finalUri = UriComponentsBuilder
                .fromUriString(uri)
                .queryParam("address", endereco)
                .queryParam("key", apikey)
                .toUriString();

        ResponseEntity<JsonNode> response =
                restTemplate.getForEntity(finalUri, JsonNode.class);

        JsonNode body = response.getBody();

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
    }

    @Override
    public AutoCompleteResponse autocomplete(String text) throws IOException, InterruptedException {

//        Dotenv dotenv = Dotenv.load();
//        String apikey = System.getenv("GOOGLE_API_KEY");
        AutoCompleteRequest requestBody = new AutoCompleteRequest(
                text,
                List.of("br")
        );

        return webClient
            .post() // agora é POST
            .uri("/v1/places:autocomplete")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Goog-Api-Key", apiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(AutoCompleteResponse.class)
            .block();
    }
}
