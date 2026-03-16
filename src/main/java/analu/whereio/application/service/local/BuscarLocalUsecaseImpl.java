package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.application.ports.in.local.BuscarLocalUsecase;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class BuscarLocalUsecaseImpl implements BuscarLocalUsecase {

    private final LatitudeLongitudeInterfacePort latitudeLongitudeInterfacePort;

    @Override
    public AutoCompleteResponse execute(String input) {

        try{
            return latitudeLongitudeInterfacePort.autocomplete(input);

        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao buscar o local", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
