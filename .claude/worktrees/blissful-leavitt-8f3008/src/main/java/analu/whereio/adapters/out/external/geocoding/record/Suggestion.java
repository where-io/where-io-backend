package analu.whereio.adapters.out.external.geocoding.record;

/**
 * Predição do Places Autocomplete (REST), já normalizada para o app.
 */
public record Suggestion(
        String placeId,
        String description,
        String mainText,
        String secondaryText
) {}
