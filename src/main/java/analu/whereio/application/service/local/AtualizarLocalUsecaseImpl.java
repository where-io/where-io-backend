package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.LatitudeLongitudeRecord;
import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.AtualizarLocalUsecase;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class AtualizarLocalUsecaseImpl implements AtualizarLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(AtualizarLocalUsecaseImpl.class);

    private final LocalRepositoryPort localRepositoryPort;
    private final LatitudeLongitudeInterfacePort latitudeLongitudePort;
    private final SincronizarTagsDoLocalService sincronizarTagsDoLocalService;
    private final VisitaRepositoryPort visitaRepositoryPort;

    @Override
    public void execute(Local local, String id, String ownerUserId) {

        MDC.put("operation", "atualizarLocal");
        try {
            log.info("Iniciando atualizacao de local. id={}", id);

            Local existente = localRepositoryPort.buscarPorIdLocal(id);
            if (isNull(existente) || existente.getOwnerUserId() == null || !existente.getOwnerUserId().equals(ownerUserId)) {
                BusinessException ex = new BusinessException("ID de local não existe", HttpStatus.NOT_FOUND);
                log.warn("Local nao encontrado para atualizacao. id={}", id);
                throw ex;
            }

            local.setId(id);
            local.setOwnerUserId(existente.getOwnerUserId());

            if (local.getVisitacao() == null) {
                local.setVisitacao(existente.getVisitacao());
            }

            if (Boolean.FALSE.equals(local.getVisitacao())) {
                var visitas = visitaRepositoryPort.buscarVisitasPorIdLocal(id, ownerUserId);
                if (visitas != null && !visitas.isEmpty()) {
                    throw new BusinessException(
                            "Local com visitas não pode ter a visitação desabilitada",
                            HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }

            if (local.getImagemUrl() == null) {
                local.setImagemUrl(existente.getImagemUrl());
            } else if (local.getImagemUrl().isBlank()) {
                local.setImagemUrl(null);
            }

            /*
             * PUT não envia {@code fotos}; MapStruct/Jackson deixa lista vazia, não null.
             * Preservar galeria já persistida (upload/remove de foto usa {@code atualizarLocal} direto no repositório).
             */
            if (local.getFotos() == null || local.getFotos().isEmpty()) {
                local.setFotos(
                        existente.getFotos() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(existente.getFotos()));
            }

            sincronizarTagsDoLocalService.aplicar(local);

            if (coordenadasValidas(local.getCoordenadas())) {
                log.debug("Atualizacao de local usando coordenadas enviadas na requisicao. id={}", id);
            } else {
                try {
                    LatitudeLongitudeRecord record =
                            latitudeLongitudePort.ConverterEnderecoParaCoordenadas(local.getEndereco().toString());

                    Coordenadas coordenadas = Coordenadas.builder().build();
                    coordenadas.setLatitude(record.latitude());
                    coordenadas.setLongitude(record.longitude());
                    local.setCoordenadas(coordenadas);

                } catch (RuntimeException | IOException | InterruptedException e) {
                    if (coordenadasValidas(existente.getCoordenadas())) {
                        local.setCoordenadas(existente.getCoordenadas());
                        log.warn(
                                "Geocoding falhou na atualizacao; mantendo coordenadas ja persistidas. id={}",
                                id,
                                e);
                    } else {
                        log.warn("Falha ao obter coordenadas na atualizacao do local. id={}", id, e);
                        throw new BusinessException(
                                "Não foi possível obter coordenadas para o endereço informado",
                                HttpStatus.UNPROCESSABLE_ENTITY);
                    }
                }
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

    private static boolean coordenadasValidas(Coordenadas c) {
        if (c == null) {
            return false;
        }
        String lat = c.getLatitude();
        String lng = c.getLongitude();
        return lat != null
                && !lat.isBlank()
                && lng != null
                && !lng.isBlank();
    }
}
