package analu.whereio.application.service.visita;

import analu.whereio.adapters.in.web.converter.VisitaConverter;
import analu.whereio.adapters.in.web.dto.response.VisitaDtoResponse;
import analu.whereio.application.ports.in.visita.BuscarVisitaPorIdLocalUsecase;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BuscarVisitaPorIdLocalUsecaseImpl implements BuscarVisitaPorIdLocalUsecase {

    private final VisitaRepositoryPort visitaRepositoryPort;
    private final VisitaConverter visitaConverter;

    @Override
    public List<VisitaDtoResponse> execute(String idLocal) {

        try{
            return visitaRepositoryPort
                    .buscarVisitasPorIdLocal(idLocal)
                    .stream()
                    .map(visitaConverter::toResponse)
                    .toList();

        }catch (Exception e){
            throw new BusinessException("Ocorreu um erro ao buscar as visitas por id do local", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
