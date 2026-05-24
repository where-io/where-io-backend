package analu.whereio.application.service.usuario;

import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoverFotoPerfilUsecaseImpl")
class RemoverFotoPerfilUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private RemoverFotoPerfilUsecaseImpl usecase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deve deletar arquivo e limpar campo fotoPerfil")
        void deveDeletarArquivoELimparCampo() throws Exception {
            UserAccount conta = conta(USER_ID, "foto.jpg");
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(conta));
            when(userAccountRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            usecase.execute(USER_ID);

            verify(fileStoragePort).deletar("foto.jpg");
            ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountRepositoryPort).save(captor.capture());
            assertNull(captor.getValue().getFotoPerfil());
        }

        @Test
        @DisplayName("deve ser no-op quando usuário não tem foto")
        void deveSerNoOpSemFoto() throws Exception {
            UserAccount conta = conta(USER_ID, null);
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(conta));

            usecase.execute(USER_ID);

            verify(fileStoragePort, never()).deletar(any());
            verify(userAccountRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND para usuário inexistente")
        void deveLancarNotFoundParaUsuarioInexistente() {
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(USER_ID));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }
    }

    private static UserAccount conta(String id, String fotoPerfil) {
        UserAccount u = new UserAccount();
        u.setId(id);
        u.setNomeUsuario("user123");
        u.setFotoPerfil(fotoPerfil);
        return u;
    }
}
