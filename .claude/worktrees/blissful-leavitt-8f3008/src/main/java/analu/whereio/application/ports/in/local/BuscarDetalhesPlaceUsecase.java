package analu.whereio.application.ports.in.local;

import analu.whereio.adapters.out.external.geocoding.record.PlaceDetailsRecord;

public interface BuscarDetalhesPlaceUsecase {

    PlaceDetailsRecord execute(String placeId);
}
