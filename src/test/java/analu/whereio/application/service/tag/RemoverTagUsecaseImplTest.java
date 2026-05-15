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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoverTagUsecaseImpl")
class RemoverTagUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private RemoverTagUsecaseImpl removerTagUsecaseImpl;

    private Tag tagExistente;
    private static final String ID_VALIDO = "tag-id-1";

    @BeforeEach
    void setUp() {
        tagExistente = new Tag();
        tagExistente.setId(ID_VALIDO);
        tagExistente.setNome("Italiano");
        tagExistente.setUserId(USER_ID);
    }

    @Nested
    @DisplayName("Quando a tag existe e pode ser removida")
    class QuandoTagExiste {

        @Test
        @DisplayName("deve chamar removerTagPorId quando a tag existe no repositório")
        void deveRemoverTagComSucesso() {
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID)).thenReturn(tagExistente);

            removerTagUsecaseImpl.execute(ID_VALIDO, USER_ID);

            verify(tagRepositoryPort).buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID);
            verify(tagRepositoryPort).removerTagPorId(ID_VALIDO);
        }
    }

    @Nested
    @DisplayName("Quando a tag não é encontrada pelo id")
    class QuandoTagNaoEncontrada {

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando o id não existe no repositório")
        void deveLancarBusinessExceptionQuandoIdNaoExiste() {
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID)).thenReturn(null);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> removerTagUsecaseImpl.execute(ID_VALIDO, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Tag não encontrada", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve não chamar removerTagPorId quando o id não existe")
        void naoDeveRemoverTagQuandoIdNaoExiste() {
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID)).thenReturn(null);

            assertThrows(
                    BusinessException.class,
                    () -> removerTagUsecaseImpl.execute(ID_VALIDO, USER_ID)
            );

            verify(tagRepositoryPort, never()).removerTagPorId(any());
        }
    }

    @Nested
    @DisplayName("Quando a persistência falha ao remover")
    class QuandoPersistenciaFalha {

        @Test
        @DisplayName("deve lançar BusinessException com INTERNAL_SERVER_ERROR quando removerTagPorId lança Exception")
        void deveLancarBusinessExceptionQuandoPersistenciaFalha() {
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID)).thenReturn(tagExistente);
            doThrow(new RuntimeException("Falha no banco de dados"))
                    .when(tagRepositoryPort).removerTagPorId(ID_VALIDO);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> removerTagUsecaseImpl.execute(ID_VALIDO, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao remover a tag", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
