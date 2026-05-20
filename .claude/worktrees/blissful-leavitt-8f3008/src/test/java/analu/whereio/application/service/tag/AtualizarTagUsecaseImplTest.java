package analu.whereio.application.service.tag;

import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.out.TagRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarTagUsecaseImpl")
class AtualizarTagUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private AtualizarTagUsecaseImpl atualizarTagUsecaseImpl;

    private Tag tagExistente;
    private Tag tagParaAtualizar;
    private static final String ID_VALIDO = "tag-id-1";

    @BeforeEach
    void setUp() {
        tagExistente = new Tag();
        tagExistente.setId(ID_VALIDO);
        tagExistente.setNome("Italiano");
        tagExistente.setUserId(USER_ID);

        tagParaAtualizar = new Tag();
        tagParaAtualizar.setNome("Italiano Atualizado");
    }

    @Nested
    @DisplayName("Quando o ID existe no repositório")
    class QuandoIdExiste {

        @Test
        @DisplayName("deve definir o id na tag e chamar atualizarTag quando o id existe")
        void deveDefinirIdEChamarAtualizarTagComSucesso() {
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID)).thenReturn(tagExistente);

            atualizarTagUsecaseImpl.execute(tagParaAtualizar, ID_VALIDO, USER_ID);

            assertAll(
                    () -> assertEquals(ID_VALIDO, tagParaAtualizar.getId()),
                    () -> assertEquals(USER_ID, tagParaAtualizar.getUserId())
            );
            verify(tagRepositoryPort).buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID);
            verify(tagRepositoryPort).atualizarTag(tagParaAtualizar);
        }
    }

    @Nested
    @DisplayName("Quando o ID não existe no repositório")
    class QuandoIdNaoExiste {

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando o id não está cadastrado")
        void deveLancarBusinessExceptionQuandoIdNaoExiste() {
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID)).thenReturn(null);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> atualizarTagUsecaseImpl.execute(tagParaAtualizar, ID_VALIDO, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Tag não encontrada", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve não chamar atualizarTag quando o id não existe")
        void naoDeveAtualizarTagQuandoIdNaoExiste() {
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID)).thenReturn(null);

            assertThrows(
                    BusinessException.class,
                    () -> atualizarTagUsecaseImpl.execute(tagParaAtualizar, ID_VALIDO, USER_ID)
            );

            verify(tagRepositoryPort, never()).atualizarTag(any());
        }
    }

    @Nested
    @DisplayName("Quando a persistência falha ao atualizar")
    class QuandoPersistenciaFalha {

        @Test
        @DisplayName("deve lançar BusinessException com INTERNAL_SERVER_ERROR quando atualizarTag lança Exception")
        void deveLancarBusinessExceptionQuandoPersistenciaFalha() {
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID)).thenReturn(tagExistente);
            doThrow(new RuntimeException("Falha no banco de dados"))
                    .when(tagRepositoryPort).atualizarTag(any());

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> atualizarTagUsecaseImpl.execute(tagParaAtualizar, ID_VALIDO, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao atualizar a tag", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
