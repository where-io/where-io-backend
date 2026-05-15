package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.ApiResponse;
import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.CadastrarLocalUsecase;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class CadastrarLocalUsecaseImpl implements CadastrarLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(CadastrarLocalUsecaseImpl.class);

    private final LocalRepositoryPort localRepositoryPort;
    private final LatitudeLongitudeInterfacePort latitudeLongitudePort;
    private final SincronizarTagsDoLocalService sincronizarTagsDoLocalService;

    @Override
    public Local execute(Local local) {

        MDC.put("operation", "cadastrarLocal");
        try {
            log.info("Iniciando cadastro de local. nome={}", local.getNome());

            if (local.getOwnerUserId() == null || local.getOwnerUserId().isBlank()) {
                throw new BusinessException("Usuário proprietário do local é obrigatório", HttpStatus.BAD_REQUEST);
            }

            if (!isNull(localRepositoryPort.buscarPorNomeLocal(local.getNome(), local.getOwnerUserId()))) {
                log.warn("Tentativa de cadastro de local duplicado. nome={}", local.getNome());
                throw new BusinessException("Local já foi cadastrado", HttpStatus.UNPROCESSABLE_CONTENT);
            }

            if (!isNull(localRepositoryPort.buscarPorCep(local.getEndereco().getCep(), local.getOwnerUserId()))) {
                log.warn("Tentativa de cadastro de local duplicado. nome={}", local.getNome());
                throw new BusinessException("Local já foi cadastrado", HttpStatus.UNPROCESSABLE_CONTENT);
            }

            sincronizarTagsDoLocalService.aplicar(local);

            if(isNull(local.getCoordenadas().getLongitude()) || isNull(local.getCoordenadas().getLatitude())){
                log.debug("Coordenadas ausentes, buscando via API. nome={}", local.getNome());
                try {
                    ApiResponse record = latitudeLongitudePort.buscarLocalizacao(local.getEndereco().toString());

                    Coordenadas coordenadas = Coordenadas.builder().build();

                    coordenadas.setLatitude((String.valueOf(record.getResults().get(0).getNavigationPoints().get(0).getLocation().getLatitude())));
                    coordenadas.setLongitude((String.valueOf(record.getResults().get(0).getNavigationPoints().get(0).getLocation().getLongitude())));

                    local.setCoordenadas(coordenadas);
                    log.debug("Coordenadas obtidas com sucesso. latitude={} longitude={}", coordenadas.getLatitude(), coordenadas.getLongitude());

                } catch (RuntimeException | IOException | InterruptedException e) {
                    throw new BusinessException("Ocorreu um erro ao cadastrar o local", HttpStatus.NOT_FOUND);
                }
            }

            try{
                Local salvo = localRepositoryPort.cadastrarLocal(local);
                MDC.put("entityId", salvo.getId());
                log.info("Local cadastrado com sucesso. id={}", salvo.getId());
                return salvo;

            }catch (Exception e){
                throw new BusinessException("Ocorreu um erro ao cadastrar o local", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
