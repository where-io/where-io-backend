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
@DisplayName("BuscarTagPorIdUsecaseImpl")
class BuscarTagPorIdUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private BuscarTagPorIdUsecaseImpl buscarTagPorIdUsecaseImpl;

    private Tag tag;
    private static final String ID_VALIDO = "tag-id-1";

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setId(ID_VALIDO);
        tag.setNome("Italiano");
    }

    @Nested
    @DisplayName("Quando a tag é encontrada pelo id")
    class QuandoTagEncontrada {

        @Test
        @DisplayName("deve retornar a tag quando o id existe no repositório")
        void deveRetornarTagQuandoIdExiste() {
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID)).thenReturn(tag);

            Tag resultado = buscarTagPorIdUsecaseImpl.execute(ID_VALIDO, USER_ID);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(ID_VALIDO, resultado.getId()),
                    () -> assertEquals("Italiano", resultado.getNome())
            );
            verify(tagRepositoryPort).buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID);
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
                    () -> buscarTagPorIdUsecaseImpl.execute(ID_VALIDO, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Tag não encontrada", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
            verify(tagRepositoryPort).buscarPorIdTagDoUsuario(ID_VALIDO, USER_ID);
        }
    }
}
