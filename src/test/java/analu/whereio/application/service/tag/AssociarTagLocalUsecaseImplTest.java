package analu.whereio.application.service.tag;

import analu.whereio.application.model.Local;
import analu.whereio.application.model.Tag;
import analu.whereio.application.ports.out.LocalRepositoryPort;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssociarTagLocalUsecaseImpl")
class AssociarTagLocalUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private LocalRepositoryPort localRepositoryPort;
    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private AssociarTagLocalUsecaseImpl associarTagLocalUsecaseImpl;

    private static final String ID_LOCAL = "local-id-1";
    private static final String ID_TAG = "tag-id-1";
    private Local local;
    private Tag tag;

    @BeforeEach
    void setUp() {
        local = new Local();
        local.setId(ID_LOCAL);
        local.setOwnerUserId(USER_ID);
        local.setIdTags(new java.util.ArrayList<>());
        tag = new Tag();
        tag.setId(ID_TAG);
        tag.setUserId(USER_ID);
    }

    @Nested
    @DisplayName("Quando a associação ainda não existe")
    class QuandoAssociacaoNaoExiste {

        @Test
        @DisplayName("deve criar a associação com idLocal e idTag corretos quando não existe associação prévia")
        void deveCriarAssociacaoComIdLocalEIdTagCorretos() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(local);
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_TAG, USER_ID)).thenReturn(tag);

            associarTagLocalUsecaseImpl.execute(ID_LOCAL, ID_TAG, USER_ID);

            assertAll(
                    () -> assertEquals(ID_LOCAL, local.getId()),
                    () -> assertEquals(List.of(ID_TAG), local.getIdTags())
            );
            verify(localRepositoryPort).atualizarLocal(local);
        }

        @Test
        @DisplayName("deve chamar buscarPorIdLocal e buscarPorIdTagDoUsuario antes de associar")
        void deveChamarBuscarAntesDeAssociar() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(local);
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_TAG, USER_ID)).thenReturn(tag);

            associarTagLocalUsecaseImpl.execute(ID_LOCAL, ID_TAG, USER_ID);

            verify(localRepositoryPort).buscarPorIdLocal(ID_LOCAL);
            verify(tagRepositoryPort).buscarPorIdTagDoUsuario(ID_TAG, USER_ID);
            verify(localRepositoryPort).atualizarLocal(local);
        }
    }

    @Nested
    @DisplayName("Quando o local não pertence ao usuário")
    class QuandoLocalNaoPertenceAoUsuario {

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando local existe mas pertence a outro usuário")
        void deveLancarNotFoundQuandoLocalPertenceAOutroUsuario() {
            // TODO: scaffold — security boundary: local exists but ownerUserId != userId
            // Setup: local.setOwnerUserId("outro-user"), when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(local)
            // Assert: assertThrows(BusinessException.class, ...) with HttpStatus.NOT_FOUND
            // Verify: verify(tagRepositoryPort, never()).buscarPorIdTagDoUsuario(any(), any())
            // Verify: verify(localRepositoryPort, never()).atualizarLocal(any())
        }
    }

    @Nested
    @DisplayName("Quando a associação já existe")
    class QuandoAssociacaoJaExiste {

        @Test
        @DisplayName("deve lançar BusinessException com UNPROCESSABLE_ENTITY quando a associação já existe")
        void deveLancarBusinessExceptionQuandoAssociacaoJaExiste() {
            local.setIdTags(new java.util.ArrayList<>(List.of(ID_TAG)));
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(local);
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_TAG, USER_ID)).thenReturn(tag);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> associarTagLocalUsecaseImpl.execute(ID_LOCAL, ID_TAG, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Associação entre local e tag já existe", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve não chamar associar quando a associação já existe")
        void naoDeveChamarAssociarQuandoAssociacaoJaExiste() {
            local.setIdTags(new java.util.ArrayList<>(List.of(ID_TAG)));
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(local);
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_TAG, USER_ID)).thenReturn(tag);

            assertThrows(
                    BusinessException.class,
                    () -> associarTagLocalUsecaseImpl.execute(ID_LOCAL, ID_TAG, USER_ID)
            );

            verify(localRepositoryPort, never()).atualizarLocal(any());
        }
    }

    @Nested
    @DisplayName("Quando a persistência falha ao associar")
    class QuandoPersistenciaFalha {

        @Test
        @DisplayName("deve lançar BusinessException com INTERNAL_SERVER_ERROR quando associar lança Exception")
        void deveLancarBusinessExceptionQuandoAssociarFalha() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(local);
            when(tagRepositoryPort.buscarPorIdTagDoUsuario(ID_TAG, USER_ID)).thenReturn(tag);
            doThrow(new RuntimeException("Falha no banco de dados"))
                    .when(localRepositoryPort).atualizarLocal(local);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> associarTagLocalUsecaseImpl.execute(ID_LOCAL, ID_TAG, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao associar a tag ao local", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
