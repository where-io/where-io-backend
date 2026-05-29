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
@DisplayName("CadastrarTagUsecaseImpl")
class CadastrarTagUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private CadastrarTagUsecaseImpl cadastrarTagUsecaseImpl;

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setNome("Italiano");
        tag.setUserId(USER_ID);
    }

    @Nested
    @DisplayName("Quando a tag ainda não está cadastrada")
    class QuandoTagNaoExiste {

        @Test
        @DisplayName("deve cadastrar e retornar a tag salva quando o nome não está duplicado")
        void deveCadastrarTagERetornarTagSalva() {
            Tag tagSalva = new Tag();
            tagSalva.setId("tag-id-gerado");
            tagSalva.setNome("Italiano");

            when(tagRepositoryPort.buscarPorNomeTag(tag.getNome(), USER_ID)).thenReturn(null);
            when(tagRepositoryPort.cadastrarTag(tag)).thenReturn(tagSalva);

            Tag resultado = cadastrarTagUsecaseImpl.execute(tag);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals("tag-id-gerado", resultado.getId()),
                    () -> assertEquals("Italiano", resultado.getNome())
            );
            verify(tagRepositoryPort).buscarPorNomeTag(tag.getNome(), USER_ID);
            verify(tagRepositoryPort).cadastrarTag(tag);
        }
    }

    @Nested
    @DisplayName("Quando o nome da tag já está cadastrado")
    class QuandoNomeJaExiste {

        @Test
        @DisplayName("deve lançar BusinessException com UNPROCESSABLE_ENTITY quando o nome já está cadastrado")
        void deveLancarBusinessExceptionQuandoNomeJaExiste() {
            when(tagRepositoryPort.buscarPorNomeTag(tag.getNome(), USER_ID)).thenReturn(tag);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarTagUsecaseImpl.execute(tag)
            );

            assertAll(
                    () -> assertEquals("Tag já foi cadastrada", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve não chamar cadastrarTag quando o nome já existe")
        void naoDeveChamarCadastrarTagQuandoNomeJaExiste() {
            when(tagRepositoryPort.buscarPorNomeTag(tag.getNome(), USER_ID)).thenReturn(tag);

            assertThrows(
                    BusinessException.class,
                    () -> cadastrarTagUsecaseImpl.execute(tag)
            );

            verify(tagRepositoryPort, never()).cadastrarTag(any());
        }
    }

    @Nested
    @DisplayName("Quando o userId da tag é inválido")
    class QuandoUserIdInvalido {

        @Test
        @DisplayName("deve lançar BusinessException com BAD_REQUEST quando userId é nulo")
        void deveLancarBadRequestQuandoUserIdNulo() {
            // TODO: scaffold — test that null userId throws BusinessException(BAD_REQUEST)
            // Setup: tag.setUserId(null)
            // Assert: assertThrows(BusinessException.class, ...) with HttpStatus.BAD_REQUEST
            // Verify: verify(tagRepositoryPort, never()).buscarPorNomeTag(any(), any())
        }

        @Test
        @DisplayName("deve lançar BusinessException com BAD_REQUEST quando userId é em branco")
        void deveLancarBadRequestQuandoUserIdEmBranco() {
            // TODO: scaffold — test that blank userId ("  ") throws BusinessException(BAD_REQUEST)
            // Setup: tag.setUserId("  ")
            // Assert: assertThrows(BusinessException.class, ...) with HttpStatus.BAD_REQUEST
            // Verify: verify(tagRepositoryPort, never()).buscarPorNomeTag(any(), any())
        }
    }

    @Nested
    @DisplayName("Quando a persistência falha ao cadastrar")
    class QuandoPersistenciaFalha {

        @Test
        @DisplayName("deve lançar BusinessException com INTERNAL_SERVER_ERROR quando cadastrarTag lança Exception")
        void deveLancarBusinessExceptionQuandoCadastrarTagFalha() {
            when(tagRepositoryPort.buscarPorNomeTag(tag.getNome(), USER_ID)).thenReturn(null);
            when(tagRepositoryPort.cadastrarTag(tag))
                    .thenThrow(new RuntimeException("Falha no banco de dados"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarTagUsecaseImpl.execute(tag)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao cadastrar a tag", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
