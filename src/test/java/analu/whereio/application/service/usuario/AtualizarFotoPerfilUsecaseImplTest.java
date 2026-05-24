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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarFotoPerfilUsecaseImpl")
class AtualizarFotoPerfilUsecaseImplTest {

    private static final String USER_ID = "user-1";

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @InjectMocks
    private AtualizarFotoPerfilUsecaseImpl usecase;

    private MultipartFile arquivoValido() {
        return new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Nested
    @DisplayName("Validação")
    class Validacao {

        @Test
        @DisplayName("deve lançar BAD_REQUEST para arquivo vazio")
        void arquivoVazio() throws Exception {
            MultipartFile vazio = new MockMultipartFile("file", "x.jpg", "image/jpeg", new byte[]{});

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(vazio, USER_ID));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            verify(fileStoragePort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND para usuário inexistente")
        void usuarioInexistente() {
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(arquivoValido(), USER_ID));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }
    }

    @Nested
    @DisplayName("Sucesso")
    class Sucesso {

        @Test
        @DisplayName("deve salvar arquivo e persistir key no usuário")
        void deveSalvarArquivoEPersistirKey() throws Exception {
            UserAccount conta = conta(USER_ID, null);
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(conta));
            when(fileStoragePort.salvar(any())).thenReturn("nova_foto.jpg");
            when(userAccountRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            String key = usecase.execute(arquivoValido(), USER_ID);

            assertEquals("nova_foto.jpg", key);
            ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountRepositoryPort).save(captor.capture());
            assertEquals("nova_foto.jpg", captor.getValue().getFotoPerfil());
        }

        @Test
        @DisplayName("deve salvar nova foto antes de deletar a antiga")
        void deveSalvarNovaFotoAntesDeDeletarAntiga() throws Exception {
            UserAccount conta = conta(USER_ID, "foto_antiga.jpg");
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(conta));
            when(fileStoragePort.salvar(any())).thenReturn("nova_foto.jpg");
            when(userAccountRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            usecase.execute(arquivoValido(), USER_ID);

            var inOrder = inOrder(fileStoragePort);
            inOrder.verify(fileStoragePort).salvar(any());
            inOrder.verify(fileStoragePort).deletar("foto_antiga.jpg");
        }
    }

    @Nested
    @DisplayName("Tratamento de erros")
    class TratamentoDeErros {

        @Test
        @DisplayName("salvarLancaIoException_deveLancar500")
        void salvarLancaIoException_deveLancar500() throws Exception {
            UserAccount conta = conta(USER_ID, null);
            when(userAccountRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(conta));
            when(fileStoragePort.salvar(any())).thenThrow(new IOException("fail"));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(arquivoValido(), USER_ID));

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
            verify(userAccountRepositoryPort, never()).save(any());
            verify(fileStoragePort, never()).deletar(any());
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
