package analu.whereio.application.service.local;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.service.files.FileStorageService;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdicionarFotoLocalUsecaseImpl")
class AdicionarFotoLocalUsecaseImplTest {

    private static final String OWNER_ID = "owner-1";
    private static final String LOCAL_ID = "local-1";

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @InjectMocks
    private AdicionarFotoLocalUsecaseImpl usecase;

    private Local local;

    @BeforeEach
    void setUp() {
        local = new Local();
        local.setId(LOCAL_ID);
        local.setOwnerUserId(OWNER_ID);
        local.setNome("Rest");
    }

    private MultipartFile arquivoValido() {
        return new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Nested
    class Validacao {

        @Test
        void arquivoVazio() throws Exception {
            MultipartFile vazio = new MockMultipartFile("file", "x.jpg", "image/jpeg", new byte[]{});
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(vazio, LOCAL_ID, OWNER_ID));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            verify(fileStorageService, never()).saveFile(any());
        }

        @Test
        void idLocalBranco() throws Exception {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(arquivoValido(), "  ", OWNER_ID));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            verify(fileStorageService, never()).saveFile(any());
        }

        @Test
        void localInexistente() throws Exception {
            when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(arquivoValido(), LOCAL_ID, OWNER_ID));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
            verify(fileStorageService, never()).saveFile(any());
        }

        @Test
        void outroDono() throws Exception {
            local.setOwnerUserId("outro");
            when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(local);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> usecase.execute(arquivoValido(), LOCAL_ID, OWNER_ID));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
            verify(fileStorageService, never()).saveFile(any());
        }
    }

    @Nested
    class Sucesso {

        @Test
        void fluxoCompleto() throws Exception {
            when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(local);
            when(fileStorageService.saveFile(any())).thenReturn("uuid_foto.jpg");

            String nome = usecase.execute(arquivoValido(), LOCAL_ID, OWNER_ID);

            assertEquals("uuid_foto.jpg", nome);

            ArgumentCaptor<Local> captor = ArgumentCaptor.forClass(Local.class);
            verify(localRepositoryPort).atualizarLocal(captor.capture());
            Local salvo = captor.getValue();
            assertEquals(1, salvo.getFotos().size());
            assertEquals("uuid_foto.jpg", salvo.getFotos().get(0));
        }

        @Test
        void listaExistente() throws Exception {
            local.setFotos(new ArrayList<>(java.util.List.of("a.jpg")));
            when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(local);
            when(fileStorageService.saveFile(any())).thenReturn("b.jpg");

            usecase.execute(arquivoValido(), LOCAL_ID, OWNER_ID);

            ArgumentCaptor<Local> captor = ArgumentCaptor.forClass(Local.class);
            verify(localRepositoryPort).atualizarLocal(captor.capture());
            assertEquals(java.util.List.of("a.jpg", "b.jpg"), captor.getValue().getFotos());
        }
    }
}
