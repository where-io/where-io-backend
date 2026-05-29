package analu.whereio.application.service.tag;

import analu.whereio.application.model.Local;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoverAssociacaoTagLocalUsecaseImpl")
class RemoverAssociacaoTagLocalUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private RemoverAssociacaoTagLocalUsecaseImpl removerAssociacaoTagLocalUsecaseImpl;

    private Local localComTag;
    private static final String ID_LOCAL = "local-id-1";
    private static final String ID_TAG = "tag-id-1";

    @BeforeEach
    void setUp() {
        localComTag = new Local();
        localComTag.setId(ID_LOCAL);
        localComTag.setOwnerUserId(USER_ID);
        localComTag.setIdTags(new ArrayList<>(List.of(ID_TAG)));
    }

    @Nested
    @DisplayName("Quando a associação existe e pode ser removida")
    class QuandoAssociacaoExiste {

        @Test
        @DisplayName("deve chamar removerAssociacao com idLocal e idTag quando a associação existe")
        void deveRemoverAssociacaoComSucesso() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(localComTag);

            removerAssociacaoTagLocalUsecaseImpl.execute(ID_LOCAL, ID_TAG, USER_ID);

            assertTrue(localComTag.getIdTags().isEmpty());
            verify(localRepositoryPort).buscarPorIdLocal(ID_LOCAL);
            verify(localRepositoryPort).atualizarLocal(localComTag);
        }
    }

    @Nested
    @DisplayName("Quando o local não pertence ao usuário")
    class QuandoLocalNaoPertenceAoUsuario {

        @Test
        @DisplayName("deve lançar NOT_FOUND quando local existe mas ownerUserId não corresponde")
        void deveLancarNotFoundQuandoOwnerDiferente() {
            // TODO: scaffold — security boundary: local exists but ownerUserId != userId
            // Setup: localComTag.setOwnerUserId("outro-user")
            // when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(localComTag)
            // Assert: assertThrows(BusinessException.class, ...) with HttpStatus.NOT_FOUND
            // Verify: verify(localRepositoryPort, never()).atualizarLocal(any())
        }
    }

    @Nested
    @DisplayName("Quando a associação não é encontrada")
    class QuandoAssociacaoNaoEncontrada {

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando a associação não existe")
        void deveLancarBusinessExceptionQuandoAssociacaoNaoExiste() {
            Local localSemTag = new Local();
            localSemTag.setId(ID_LOCAL);
            localSemTag.setOwnerUserId(USER_ID);
            localSemTag.setIdTags(List.of());
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(localSemTag);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> removerAssociacaoTagLocalUsecaseImpl.execute(ID_LOCAL, ID_TAG, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Associação entre local e tag não encontrada", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve não chamar removerAssociacao quando a associação não existe")
        void naoDeveRemoverAssociacaoQuandoNaoExiste() {
            Local localSemTag = new Local();
            localSemTag.setId(ID_LOCAL);
            localSemTag.setOwnerUserId(USER_ID);
            localSemTag.setIdTags(List.of());
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(localSemTag);

            assertThrows(
                    BusinessException.class,
                    () -> removerAssociacaoTagLocalUsecaseImpl.execute(ID_LOCAL, ID_TAG, USER_ID)
            );

            verify(localRepositoryPort, never()).atualizarLocal(any());
        }
    }

    @Nested
    @DisplayName("Quando a persistência falha ao remover")
    class QuandoPersistenciaFalha {

        @Test
        @DisplayName("deve lançar BusinessException com INTERNAL_SERVER_ERROR quando removerAssociacao lança Exception")
        void deveLancarBusinessExceptionQuandoPersistenciaFalha() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(localComTag);
            doThrow(new RuntimeException("Falha no banco de dados"))
                    .when(localRepositoryPort).atualizarLocal(localComTag);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> removerAssociacaoTagLocalUsecaseImpl.execute(ID_LOCAL, ID_TAG, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao remover a associação", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
