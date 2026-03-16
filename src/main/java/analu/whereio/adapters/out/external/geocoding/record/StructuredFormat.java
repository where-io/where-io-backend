package analu.whereio.adapters.out.external.geocoding.record;

public record StructuredFormat(
        PlaceText mainText,
        PlaceText secondaryText
) {}