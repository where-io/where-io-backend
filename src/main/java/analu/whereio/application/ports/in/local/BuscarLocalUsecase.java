package analu.whereio.application.ports.in.local;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;

public interface BuscarLocalUsecase {

    AutoCompleteResponse execute(String input);

}
