package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.ApiResponse;
import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.CadastrarLocalUsecase;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class CadastrarLocalUsecaseImpl implements CadastrarLocalUsecase {

    private final LocalRepositoryPort localRepositoryPort;
    private final LatitudeLongitudeInterfacePort latitudeLongitudePort;

    @Override
    public Local execute(Local local) {

        if(!isNull(localRepositoryPort.buscarPorNomeLocal(local.getNome()))){
            throw new BusinessException("Local já foi cadastrado", HttpStatus.UNPROCESSABLE_CONTENT);
        }

        if(isNull(local.getCoordenadas().getLongitude()) || isNull(local.getCoordenadas().getLatitude())){
            try {
                ApiResponse record = latitudeLongitudePort.buscarLocalizacao(local.getEndereco().toString());

                Coordenadas coordenadas = Coordenadas.builder().build();

                coordenadas.setLatitude((String.valueOf(record.getResults().get(0).getNavigationPoints().get(0).getLocation().getLatitude())));
                coordenadas.setLongitude((String.valueOf(record.getResults().get(0).getNavigationPoints().get(0).getLocation().getLongitude())));

                local.setCoordenadas(coordenadas);

            } catch (RuntimeException | IOException | InterruptedException e) {
                throw new BusinessException("Ocorreu um erro ao cadastrar o local", HttpStatus.NOT_FOUND);
            }
        }

        try{
            return localRepositoryPort.cadastrarLocal(local);

        }catch (Exception e){
            throw new BusinessException("Ocorreu um erro ao cadastrar o local", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
