package analu.whereio.application.service.local;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
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
@DisplayName("RemoverLocalUsecaseImpl")
class RemoverLocalUsecaseImplTest {

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @InjectMocks
    private RemoverLocalUsecaseImpl removerLocalUsecaseImpl;

    private static final String ID_VALIDO = "abc-123";
    private static final String OWNER_ID = "owner-1";

    @Nested
    @DisplayName("Quando a remoção é executada com sucesso")
    class QuandoSucesso {

        @Test
        @DisplayName("deve chamar removerLocalPorId com o id correto quando o local pertence ao usuário")
        void deveChamarRemoverLocalPorIdComIdCorretoQuandoIdValido() {
            Local local = new Local();
            local.setId(ID_VALIDO);
            local.setOwnerUserId(OWNER_ID);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(local);

            removerLocalUsecaseImpl.execute(ID_VALIDO, OWNER_ID);

            verify(localRepositoryPort).removerLocalPorId(ID_VALIDO);
            verifyNoMoreInteractions(localRepositoryPort);
        }
    }

    @Nested
    @DisplayName("Quando a remoção falha")
    class QuandoFalha {

        @Test
        @DisplayName("deve lançar BusinessException com INTERNAL_SERVER_ERROR quando removerLocalPorId lança RuntimeException")
        void deveLancarBusinessExceptionQuandoRemoverLocalPorIdFalha() {
            Local local = new Local();
            local.setId(ID_VALIDO);
            local.setOwnerUserId(OWNER_ID);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(local);
            doThrow(new RuntimeException("Falha no banco de dados"))
                    .when(localRepositoryPort).removerLocalPorId(ID_VALIDO);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> removerLocalUsecaseImpl.execute(ID_VALIDO, OWNER_ID)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao remover o local", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando o local não existe ou não pertence ao usuário")
        void deveLancarNotFoundQuandoSemPermissao() {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(null);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> removerLocalUsecaseImpl.execute(ID_VALIDO, OWNER_ID)
            );
            assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus());
            verify(localRepositoryPort, never()).removerLocalPorId(any());
        }
    }
}
