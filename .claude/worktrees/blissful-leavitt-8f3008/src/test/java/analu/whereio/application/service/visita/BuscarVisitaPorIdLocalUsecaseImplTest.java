package analu.whereio.application.service.visita;

import analu.whereio.adapters.in.web.converter.VisitaConverter;
import analu.whereio.adapters.in.web.dto.response.VisitaDtoResponse;
import analu.whereio.application.model.Local;
import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarVisitaPorIdLocalUsecaseImpl")
class BuscarVisitaPorIdLocalUsecaseImplTest {

    private static final String USER_ID = "user-abc";

    @Mock
    private VisitaRepositoryPort visitaRepositoryPort;

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @Mock
    private VisitaConverter visitaConverter;

    @InjectMocks
    private BuscarVisitaPorIdLocalUsecaseImpl buscarVisitaPorIdLocalUsecase;

    private static final String ID_LOCAL = "local-555";

    private Local localDoUsuario() {
        Local local = new Local();
        local.setId(ID_LOCAL);
        local.setOwnerUserId(USER_ID);
        return local;
    }

    @Nested
    @DisplayName("Quando existem visitas para o local")
    class QuandoExistemVisitas {

        @Test
        @DisplayName("deve retornar lista de VisitaDtoResponse mapeados pelo converter quando há visitas cadastradas")
        void deveRetornarListaDeVisitaDtoResponseQuandoHaVisitas() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(localDoUsuario());

            Visita visita1 = criarVisita("visita-1", ID_LOCAL, 5, "Excelente");
            Visita visita2 = criarVisita("visita-2", ID_LOCAL, 3, "Regular");

            VisitaDtoResponse response1 = criarVisitaDtoResponse("visita-1", ID_LOCAL, 5, "Excelente");
            VisitaDtoResponse response2 = criarVisitaDtoResponse("visita-2", ID_LOCAL, 3, "Regular");

            when(visitaRepositoryPort.buscarVisitasPorIdLocal(ID_LOCAL, USER_ID)).thenReturn(List.of(visita1, visita2));
            when(visitaConverter.toResponse(visita1)).thenReturn(response1);
            when(visitaConverter.toResponse(visita2)).thenReturn(response2);

            List<VisitaDtoResponse> resultado = buscarVisitaPorIdLocalUsecase.execute(ID_LOCAL, USER_ID);

            assertAll(
                    () -> assertEquals(2, resultado.size()),
                    () -> assertEquals("visita-1", resultado.get(0).getId()),
                    () -> assertEquals("visita-2", resultado.get(1).getId()),
                    () -> verify(visitaRepositoryPort).buscarVisitasPorIdLocal(ID_LOCAL, USER_ID),
                    () -> verify(visitaConverter).toResponse(visita1),
                    () -> verify(visitaConverter).toResponse(visita2)
            );
        }
    }

    @Nested
    @DisplayName("Quando não existem visitas para o local")
    class QuandoNaoExistemVisitas {

        @Test
        @DisplayName("deve retornar lista vazia quando não há visitas cadastradas para o local")
        void deveRetornarListaVaziaQuandoNaoHaVisitas() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(localDoUsuario());
            when(visitaRepositoryPort.buscarVisitasPorIdLocal(ID_LOCAL, USER_ID)).thenReturn(List.of());

            List<VisitaDtoResponse> resultado = buscarVisitaPorIdLocalUsecase.execute(ID_LOCAL, USER_ID);

            assertAll(
                    () -> assertTrue(resultado.isEmpty()),
                    () -> verify(visitaRepositoryPort).buscarVisitasPorIdLocal(ID_LOCAL, USER_ID)
            );
        }
    }

    @Nested
    @DisplayName("Quando o local não existe ou não pertence ao usuário")
    class QuandoLocalInvalido {

        @Test
        @DisplayName("deve lançar NOT_FOUND quando local é null")
        void deveLancarQuandoLocalNull() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(null);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> buscarVisitaPorIdLocalUsecase.execute(ID_LOCAL, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Local não encontrado", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando owner é outro usuário")
        void deveLancarQuandoOwnerDiferente() {
            Local local = new Local();
            local.setId(ID_LOCAL);
            local.setOwnerUserId("outro");
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(local);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> buscarVisitaPorIdLocalUsecase.execute(ID_LOCAL, USER_ID)
            );

            assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus());
        }
    }

    @Nested
    @DisplayName("Quando ocorre erro na busca")
    class QuandoOcorreErro {

        @Test
        @DisplayName("deve lançar BusinessException com status INTERNAL_SERVER_ERROR quando buscarVisitasPorIdLocal lança RuntimeException")
        void deveLancarBusinessExceptionQuandoBuscarVisitasFalha() {
            when(localRepositoryPort.buscarPorIdLocal(ID_LOCAL)).thenReturn(localDoUsuario());
            when(visitaRepositoryPort.buscarVisitasPorIdLocal(ID_LOCAL, USER_ID))
                    .thenThrow(new RuntimeException("erro de conexão com banco"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> buscarVisitaPorIdLocalUsecase.execute(ID_LOCAL, USER_ID)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao buscar as visitas por id do local", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }

    private Visita criarVisita(String id, String idLocal, int avaliacao, String comentario) {
        Visita visita = new Visita();
        visita.setId(id);
        visita.setIdLocal(idLocal);
        visita.setAvaliacao(avaliacao);
        visita.setComentario(comentario);
        visita.setDataVisita(LocalDate.of(2026, 4, 5));
        return visita;
    }

    private VisitaDtoResponse criarVisitaDtoResponse(String id, String idLocal, int avaliacao, String comentario) {
        VisitaDtoResponse response = new VisitaDtoResponse();
        response.setId(id);
        response.setIdLocal(idLocal);
        response.setAvaliacao(avaliacao);
        response.setComentario(comentario);
        response.setDataVisita(LocalDate.of(2026, 4, 5));
        return response;
    }
}
