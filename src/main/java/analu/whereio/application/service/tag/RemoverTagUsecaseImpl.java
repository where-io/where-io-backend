package analu.whereio.application.service.tag;

import analu.whereio.application.ports.in.tag.RemoverTagUsecase;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class RemoverTagUsecaseImpl implements RemoverTagUsecase {

    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public void execute(String id, String userId) {
        try {
            if (isNull(tagRepositoryPort.buscarPorIdTagDoUsuario(id, userId))) {
                throw new BusinessException("Tag não encontrada", HttpStatus.NOT_FOUND);
            }
            tagRepositoryPort.removerTagPorId(id);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Ocorreu um erro ao remover a tag", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
