package analu.whereio.application.service.tag;

import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.CadastrarTagUsecase;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class CadastrarTagUsecaseImpl implements CadastrarTagUsecase {

    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public Tag execute(Tag tag) {
        if (tag.getUserId() == null || tag.getUserId().isBlank()) {
            throw new BusinessException("Usuário da tag é obrigatório", HttpStatus.BAD_REQUEST);
        }

        if (!isNull(tagRepositoryPort.buscarPorNomeTag(tag.getNome(), tag.getUserId()))) {
            throw new BusinessException("Tag já foi cadastrada", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        try {
            return tagRepositoryPort.cadastrarTag(tag);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao cadastrar a tag", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
