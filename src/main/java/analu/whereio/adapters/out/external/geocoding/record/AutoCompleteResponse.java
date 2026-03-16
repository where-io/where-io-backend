package analu.whereio.adapters.out.external.geocoding.record;

import java.util.List;

public record AutoCompleteResponse (
        List<Suggestion> suggestions
){}
