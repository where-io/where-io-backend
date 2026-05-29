package analu.whereio.application.service.tag;

import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.in.tag.BuscarTagPorIdUsecase;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class BuscarTagPorIdUsecaseImpl implements BuscarTagPorIdUsecase {

    private final TagRepositoryPort tagRepositoryPort;

    @Override
    public Tag execute(String id, String userId) {
        Tag tag = tagRepositoryPort.buscarPorIdTagDoUsuario(id, userId);
        if (isNull(tag)) {
            throw new BusinessException("Tag não encontrada", HttpStatus.NOT_FOUND);
        }
        return tag;
    }
}
