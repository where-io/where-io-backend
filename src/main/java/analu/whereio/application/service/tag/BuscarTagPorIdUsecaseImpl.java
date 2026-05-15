package analu.whereio.application.service.tag;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.BuscarTagPorIdUsecase;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import static java.util.Objects.isNull;
@Service
@RequiredArgsConstructor
public class BuscarTagPorIdUsecaseImpl implements BuscarTagPorIdUsecase {
    private static final Logger log = LoggerFactory.getLogger(BuscarTagPorIdUsecaseImpl.class);
    private final TagRepositoryPort tagRepositoryPort;
    @Override
    public Tag execute(String id, String userId) {
        MDC.put("operation", "buscarTagPorId");
        MDC.put("entityId", id);
        try {
            log.info("Iniciando busca de tag. id={}", id);
            Tag tag = tagRepositoryPort.buscarPorIdTagDoUsuario(id, userId);
            if (isNull(tag)) {
                log.warn("Tag não encontrada. id={}", id);
                throw new BusinessException("Tag não encontrada", HttpStatus.NOT_FOUND);
            }
            log.info("Tag encontrada com sucesso. id={}", id);
            return tag;
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
