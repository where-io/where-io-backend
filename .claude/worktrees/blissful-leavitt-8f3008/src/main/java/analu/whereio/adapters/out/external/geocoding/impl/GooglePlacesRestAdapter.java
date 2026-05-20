package analu.whereio.adapters.out.external.geocoding.impl;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.adapters.out.external.geocoding.record.PlaceDetailsRecord;
import analu.whereio.adapters.out.external.geocoding.record.Suggestion;
import analu.whereio.application.ports.out.PlacesApiPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GooglePlacesRestAdapter implements PlacesApiPort {

    private static final Logger log = LoggerFactory.getLogger(GooglePlacesRestAdapter.class);

    private final WebClient webClient;

    @Value("${google.map.api.key}")
    private String apiKey;

    @Override
    public AutoCompleteResponse placeAutocomplete(String input, String sessionToken) {
        try {
            JsonNode body = webClient
                    .get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder
                                .scheme("https")
                                .host("maps.googleapis.com")
                                .path("/maps/api/place/autocomplete/json")
                                .queryParam("input", input)
                                .queryParam("language", "pt-BR")
                                .queryParam("components", "country:br")
                                .queryParam("key", apiKey);
                        if (sessionToken != null && !sessionToken.isBlank()) {
                            b.queryParam("sessiontoken", sessionToken);
                        }
                        return b.build();
                    })
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (body == null) {
                return new AutoCompleteResponse(List.of());
            }
            String status = body.path("status").asText("");
            if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
                log.warn("Places Autocomplete status inesperado: {} — {}", status, body);
                return new AutoCompleteResponse(List.of());
            }

            JsonNode predictions = body.path("predictions");
            if (!predictions.isArray()) {
                return new AutoCompleteResponse(List.of());
            }

            List<Suggestion> list = new ArrayList<>();
            for (JsonNode p : predictions) {
                String placeId = p.path("place_id").asText("");
                String description = p.path("description").asText("");
                JsonNode sf = p.path("structured_formatting");
                String mainText = sf.path("main_text").asText(description);
                String secondaryText = sf.path("secondary_text").asText("");
                if (!placeId.isEmpty()) {
                    list.add(new Suggestion(placeId, description, mainText, secondaryText));
                }
            }
            return new AutoCompleteResponse(list);
        } catch (Exception e) {
            log.error("Falha ao chamar Places Autocomplete. input={} erro={}", input, e.getMessage(), e);
            return new AutoCompleteResponse(List.of());
        }
    }

    @Override
    public PlaceDetailsRecord placeDetails(String placeId) {
        JsonNode body = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("maps.googleapis.com")
                        .path("/maps/api/place/details/json")
                        .queryParam("place_id", placeId)
                        .queryParam("fields", "geometry,address_components,formatted_address")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (body == null) {
            throw new IllegalStateException("Resposta vazia do Place Details");
        }
        String status = body.path("status").asText("");
        if (!"OK".equals(status)) {
            throw new IllegalStateException("Place Details status=" + status);
        }
        JsonNode r = body.path("result");
        if (r.isMissingNode() || r.isNull()) {
            throw new IllegalStateException("Place Details sem result");
        }

        JsonNode loc = r.path("geometry").path("location");
        double lat = loc.path("lat").asDouble(Double.NaN);
        double lng = loc.path("lng").asDouble(Double.NaN);
        if (Double.isNaN(lat) || Double.isNaN(lng)) {
            throw new IllegalStateException("Place Details sem coordenadas");
        }

        JsonNode components = r.path("address_components");
        String route = componentLongName(components, "route");
        String number = componentLongName(components, "street_number");
        String logradouro = List.of(route, number).stream().filter(s -> !s.isEmpty()).reduce((a, b) -> a + ", " + b).orElse("");
        String bairro = firstNonEmpty(
                componentLongName(components, "sublocality_level_1"),
                componentLongName(components, "neighborhood"));
        String cidade = componentLongName(components, "administrative_area_level_2");
        String estado = componentLongName(components, "administrative_area_level_1");
        String cep = componentLongName(components, "postal_code");
        String pais = componentLongName(components, "country");
        String formatted = r.path("formatted_address").asText("");

        return new PlaceDetailsRecord(lat, lng, logradouro, bairro, cidade, estado, cep, pais, formatted);
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.isEmpty()) return a;
        return b != null ? b : "";
    }

    private static String componentLongName(JsonNode components, String type) {
        if (components == null || !components.isArray()) {
            return "";
        }
        for (JsonNode c : components) {
            JsonNode types = c.path("types");
            if (types.isArray()) {
                for (JsonNode t : types) {
                    if (type.equals(t.asText())) {
                        return c.path("long_name").asText("");
                    }
                }
            }
        }
        return "";
    }
}
