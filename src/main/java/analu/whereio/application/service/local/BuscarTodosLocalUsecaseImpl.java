package analu.whereio.application.service.local;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.BuscarTodosLocalUsecase;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BuscarTodosLocalUsecaseImpl implements BuscarTodosLocalUsecase {

    private static final Logger log = LoggerFactory.getLogger(BuscarTodosLocalUsecaseImpl.class);

    private final LocalRepositoryPort localRepositoryPort;
    private final VisitaRepositoryPort visitaRepository;
    private final SincronizarTagsDoLocalService sincronizarTagsDoLocalService;

    @Override
    public List<Local> execute(String ownerUserId, int page, int size) {

        MDC.put("operation", "buscarTodosLocal");
        try{
            log.info("Iniciando busca de locais do usuário. ownerUserId={}", ownerUserId);
            List<Local> lista = localRepositoryPort
                    .buscarTodosLocalPorUsuarioPaginado(ownerUserId, page, size)
                    .stream()
                    .map(local ->{
                        local.setVisitas(visitaRepository.buscarVisitasPorIdLocal(local.getId(), ownerUserId));
                        sincronizarTagsDoLocalService.hidratarParaResposta(local);
                        return local;
                    })
                    .toList();

            log.info("Busca de todos os locais concluida. quantidade={}", lista.size());
            return lista;

        }catch (Exception e){
            throw new BusinessException("Ocorreu um erro ao buscar os locais", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.remove("operation");
        }
    }
}
