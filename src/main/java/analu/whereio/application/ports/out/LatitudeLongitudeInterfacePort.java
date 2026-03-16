package analu.whereio.application.ports.out;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.adapters.out.external.geocoding.record.LatitudeLongitudeRecord;

import java.io.IOException;

public interface LatitudeLongitudeInterfacePort {

    LatitudeLongitudeRecord ConverterEnderecoParaCoordenadas(String endereco) throws IOException, InterruptedException;
    AutoCompleteResponse autocomplete(String endereco) throws IOException, InterruptedException;
}
