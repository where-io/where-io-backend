package analu.whereio.application.service.tag;
import analu.whereio.application.ports.in.tag.RemoverTagUsecase;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import static java.util.Objects.isNull;
@Service
@RequiredArgsConstructor
public class RemoverTagUsecaseImpl implements RemoverTagUsecase {
    private static final Logger log = LoggerFactory.getLogger(RemoverTagUsecaseImpl.class);
    private final TagRepositoryPort tagRepositoryPort;
    @Override
    public void execute(String id, String userId) {
        MDC.put("operation", "removerTag");
        MDC.put("entityId", id);
        try {
            log.info("Iniciando remoção de tag. id={}", id);
            if (isNull(tagRepositoryPort.buscarPorIdTagDoUsuario(id, userId))) {
                log.warn("Tag não encontrada para remoção. id={}", id);
                throw new BusinessException("Tag não encontrada", HttpStatus.NOT_FOUND);
            }
            tagRepositoryPort.removerTagPorId(id);
            log.info("Tag removida com sucesso. id={}", id);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao remover a tag", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
