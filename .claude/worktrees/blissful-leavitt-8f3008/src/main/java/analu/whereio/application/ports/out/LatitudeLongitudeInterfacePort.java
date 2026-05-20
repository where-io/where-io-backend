package analu.whereio.application.ports.out;

import analu.whereio.adapters.out.external.geocoding.record.ApiResponse;
import analu.whereio.adapters.out.external.geocoding.record.LatitudeLongitudeRecord;

import java.io.IOException;

public interface LatitudeLongitudeInterfacePort {

    LatitudeLongitudeRecord ConverterEnderecoParaCoordenadas(String endereco) throws IOException, InterruptedException;
    ApiResponse buscarLocalizacao(String endereco) throws IOException, InterruptedException;
}
