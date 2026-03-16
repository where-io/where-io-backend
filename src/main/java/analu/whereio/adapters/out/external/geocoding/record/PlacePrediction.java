package analu.whereio.adapters.out.external.geocoding.record;

import java.util.List;

public record PlacePrediction(
        String place,
        String placeId,
        PlaceText text,
        StructuredFormat structuredFormat,
        List<String> types
) {}
