package analu.whereio.adapters.out.external.geocoding.record;

import java.util.List;

public record AutoCompleteRequest(
    String input,
    List<String> includedRegionCodes
) {}
