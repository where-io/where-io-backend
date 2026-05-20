package analu.whereio.application.service.tag;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.CadastrarTagUsecase;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import static java.util.Objects.isNull;
@Service
@RequiredArgsConstructor
public class CadastrarTagUsecaseImpl implements CadastrarTagUsecase {
    private static final Logger log = LoggerFactory.getLogger(CadastrarTagUsecaseImpl.class);
    private final TagRepositoryPort tagRepositoryPort;
    @Override
    public Tag execute(Tag tag) {
        MDC.put("operation", "cadastrarTag");
        try {
            log.info("Iniciando cadastro de tag. nome={}", tag.getNome());
            if (tag.getUserId() == null || tag.getUserId().isBlank()) {
                throw new BusinessException("Usuário da tag é obrigatório", HttpStatus.BAD_REQUEST);
            }
            if (!isNull(tagRepositoryPort.buscarPorNomeTag(tag.getNome(), tag.getUserId()))) {
                log.warn("Tentativa de cadastro de tag duplicada. nome={}", tag.getNome());
                throw new BusinessException("Tag já foi cadastrada", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            try {
                Tag salva = tagRepositoryPort.cadastrarTag(tag);
                MDC.put("entityId", salva.getId());
                log.info("Tag cadastrada com sucesso. id={}", salva.getId());
                return salva;
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                throw new BusinessException("Ocorreu um erro ao cadastrar a tag", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
