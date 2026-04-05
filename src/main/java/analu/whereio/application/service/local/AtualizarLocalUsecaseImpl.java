package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.LatitudeLongitudeRecord;
import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.AtualizarLocalUsecase;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class AtualizarLocalUsecaseImpl implements AtualizarLocalUsecase {

    private final LocalRepositoryPort localRepositoryPort;
    private final LatitudeLongitudeInterfacePort latitudeLongitudePort;

    @Override
    public void execute(Local local, String id) {

        if(isNull(localRepositoryPort.buscarPorIdLocal(id))){
            throw new BusinessException("ID de local não existe", HttpStatus.NOT_FOUND);
        }

        local.setId(id);

        try{
            LatitudeLongitudeRecord record = latitudeLongitudePort.ConverterEnderecoParaCoordenadas(local.getEndereco().toString());

            Coordenadas coordenadas = Coordenadas.builder().build();

            coordenadas.setLatitude(record.latitude());
            coordenadas.setLongitude(record.longitude());

            local.setCoordenadas(coordenadas);

        } catch (RuntimeException | IOException | InterruptedException e) {
            throw new BusinessException("Local não foi encontrado", HttpStatus.NOT_FOUND);
        }

        try{
            localRepositoryPort.atualizarLocal(local);

        }catch (Exception e){
            throw new BusinessException("Ocorreu um erro ao atualizar o local", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
