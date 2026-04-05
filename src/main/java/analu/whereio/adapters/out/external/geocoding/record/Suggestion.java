package analu.whereio.adapters.out.external.geocoding.record;

import java.util.List;

public record Suggestion(
        PlacePrediction placePrediction
) {

    public record PlacePrediction(
            String place,
            String placeId,
            PlaceText text,
            StructuredFormat structuredFormat,
            List<String> types
    ) {

        public record StructuredFormat(
                PlaceText mainText,
                PlaceText secondaryText
        ) {}

        public record PlaceText(
                String text,
                List<Match> matches
        ) {

            public record Match(
                    int endOffset
            ) {}
        }
    }
}
