package analu.whereio.adapters.out.external.geocoding.record;

import java.util.List;

public record PlaceText(
        String text,
        List<Match> matches
) {}
