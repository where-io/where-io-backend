package analu.whereio.application.service.local;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.service.files.FileStorageService;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoverFotoLocalUsecaseImpl")
class RemoverFotoLocalUsecaseImplTest {

    private static final String OWNER_ID = "owner-1";
    private static final String LOCAL_ID = "local-1";
    private static final String FILE = "uuid_pic.png";

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @InjectMocks
    private RemoverFotoLocalUsecaseImpl usecase;

    private Local local;

    @BeforeEach
    void setUp() {
        local = new Local();
        local.setId(LOCAL_ID);
        local.setOwnerUserId(OWNER_ID);
        local.setFotos(new ArrayList<>(java.util.List.of(FILE, "other.png")));
    }

    @Test
    void arquivoEmBranco() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> usecase.execute(LOCAL_ID, "  ", OWNER_ID));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(localRepositoryPort, never()).atualizarLocal(any());
    }

    @Test
    void localInexistente() throws IOException {
        when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usecase.execute(LOCAL_ID, FILE, OWNER_ID));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(fileStorageService, never()).deleteStoredFile(any());
    }

    @Test
    void fotoNaoEstaNaLista() throws IOException {
        when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(local);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usecase.execute(LOCAL_ID, "nao-existe.png", OWNER_ID));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(fileStorageService, never()).deleteStoredFile(any());
        verify(localRepositoryPort, never()).atualizarLocal(any());
    }

    @Test
    void removeListaEPersisteEApagaArquivo() throws IOException {
        when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(local);
        doNothing().when(fileStorageService).deleteStoredFile(FILE);

        assertDoesNotThrow(() -> usecase.execute(LOCAL_ID, FILE, OWNER_ID));

        ArgumentCaptor<Local> captor = ArgumentCaptor.forClass(Local.class);
        verify(localRepositoryPort).atualizarLocal(captor.capture());
        assertEquals(java.util.List.of("other.png"), captor.getValue().getFotos());
        verify(fileStorageService).deleteStoredFile(FILE);
    }
}
