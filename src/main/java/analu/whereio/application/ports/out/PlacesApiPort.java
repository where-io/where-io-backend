package analu.whereio.application.ports.out;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.adapters.out.external.geocoding.record.PlaceDetailsRecord;

public interface PlacesApiPort {

    AutoCompleteResponse placeAutocomplete(String input, String sessionToken);

    PlaceDetailsRecord placeDetails(String placeId);
}
