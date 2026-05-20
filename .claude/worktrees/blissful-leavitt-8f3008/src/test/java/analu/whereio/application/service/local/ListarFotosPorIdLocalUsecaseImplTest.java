package analu.whereio.application.service.local;

import analu.whereio.application.model.Local;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListarFotosPorIdLocalUsecaseImpl")
class ListarFotosPorIdLocalUsecaseImplTest {

    private static final String OWNER_ID = "owner-1";
    private static final String LOCAL_ID = "local-1";

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @InjectMocks
    private ListarFotosPorIdLocalUsecaseImpl usecase;

    private Local local;

    @BeforeEach
    void setUp() {
        local = new Local();
        local.setId(LOCAL_ID);
        local.setOwnerUserId(OWNER_ID);
    }

    @Test
    void idLocalBranco() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> usecase.execute("  ", OWNER_ID));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void localInexistente() {
        when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usecase.execute(LOCAL_ID, OWNER_ID));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void outroDono() {
        local.setOwnerUserId("outro");
        when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(local);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usecase.execute(LOCAL_ID, OWNER_ID));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void listaVazia() {
        when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(local);

        List<String> r = usecase.execute(LOCAL_ID, OWNER_ID);

        assertTrue(r.isEmpty());
    }

    @Test
    void retornaFotos() {
        local.setFotos(List.of("a.jpg", "b.jpg"));
        when(localRepositoryPort.buscarPorIdLocal(LOCAL_ID)).thenReturn(local);

        List<String> r = usecase.execute(LOCAL_ID, OWNER_ID);

        assertEquals(List.of("a.jpg", "b.jpg"), r);
    }
}
