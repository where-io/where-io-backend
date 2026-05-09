package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.LatitudeLongitudeRecord;
import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.AtualizarLocalUsecase;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class AtualizarLocalUsecaseImpl implements AtualizarLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(AtualizarLocalUsecaseImpl.class);

    private final LocalRepositoryPort localRepositoryPort;
    private final LatitudeLongitudeInterfacePort latitudeLongitudePort;
    private final SincronizarTagsDoLocalService sincronizarTagsDoLocalService;

    @Override
    public void execute(Local local, String id) {

        MDC.put("operation", "atualizarLocal");
        try {
            log.info("Iniciando atualizacao de local. id={}", id);

            if(isNull(localRepositoryPort.buscarPorIdLocal(id))){
                BusinessException ex = new BusinessException("ID de local não existe", HttpStatus.NOT_FOUND);
                log.warn("Local nao encontrado para atualizacao. id={}", id);
                throw ex;
            }

            local.setId(id);

            sincronizarTagsDoLocalService.aplicar(local);

            try{
                LatitudeLongitudeRecord record = latitudeLongitudePort.ConverterEnderecoParaCoordenadas(local.getEndereco().toString());

                Coordenadas coordenadas = Coordenadas.builder().build();

                coordenadas.setLatitude(record.latitude());
                coordenadas.setLongitude(record.longitude());

                local.setCoordenadas(coordenadas);

            } catch (RuntimeException | IOException | InterruptedException e) {
                BusinessException bex = new BusinessException("Local não foi encontrado", HttpStatus.NOT_FOUND);
                log.warn("Falha ao obter coordenadas na atualizacao do local. id={}", id);
                throw bex;
            }

            try{
                localRepositoryPort.atualizarLocal(local);

            }catch (Exception e){
                throw new BusinessException("Ocorreu um erro ao atualizar o local", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("Local atualizado com sucesso. id={}", id);

        } finally {
            MDC.remove("operation");
        }
    }
}
