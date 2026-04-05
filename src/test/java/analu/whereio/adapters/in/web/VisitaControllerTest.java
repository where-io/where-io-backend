package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.VisitaConverter;
import analu.whereio.adapters.in.web.dto.request.VisitaDtoRequest;
import analu.whereio.adapters.in.web.dto.response.VisitaDtoResponse;
import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.in.visita.AtualizarVisitaUsecase;
import analu.whereio.application.ports.in.visita.BuscarVisitaPorIdLocalUsecase;
import analu.whereio.application.ports.in.visita.CadastrarVisitaUsecase;
import analu.whereio.application.ports.in.visita.RemoverVisitaUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VisitaController")
class VisitaControllerTest {

    @Mock
    private CadastrarVisitaUsecase cadastrarVisitaUsecase;

    @Mock
    private AtualizarVisitaUsecase atualizarVisitaUsecase;

    @Mock
    private RemoverVisitaUsecase removerVisitaUsecase;

    @Mock
    private BuscarVisitaPorIdLocalUsecase buscarVisitaPorIdLocalUsecase;

    @Mock
    private VisitaConverter mapper;

    @InjectMocks
    private VisitaController visitaController;

    private Visita visita;
    private VisitaDtoRequest visitaDtoRequest;
    private VisitaDtoResponse visitaDtoResponse;

    @BeforeEach
    void setUp() {
        visita = new Visita();
        visita.setId("visita-id-1");
        visita.setDataVisita(LocalDate.of(2024, 6, 15));
        visita.setAvaliacao(4);
        visita.setComentario("Ótimo lugar!");
        visita.setIdLocal("local-id-1");

        visitaDtoRequest = new VisitaDtoRequest();
        visitaDtoRequest.setDataVisita(LocalDate.of(2024, 6, 15));
        visitaDtoRequest.setAvaliacao(4);
        visitaDtoRequest.setComentario("Ótimo lugar!");
        visitaDtoRequest.setIdLocal("local-id-1");

        visitaDtoResponse = new VisitaDtoResponse();
        visitaDtoResponse.setId("visita-id-1");
        visitaDtoResponse.setDataVisita(LocalDate.of(2024, 6, 15));
        visitaDtoResponse.setAvaliacao(4);
        visitaDtoResponse.setComentario("Ótimo lugar!");
        visitaDtoResponse.setIdLocal("local-id-1");
    }

    @Nested
    @DisplayName("GET /api/visita/{idLocal} - buscarVisitasPorIdLocal")
    class BuscarVisitasPorIdLocal {

        @Test
        @DisplayName("deve chamar o use case com o idLocal e retornar 200 OK com a lista de visitas")
        void deveBuscarVisitasPorIdLocalERetornarListaCom200() {
            String idLocal = "local-id-1";

            VisitaDtoResponse segundaVisitaResponse = new VisitaDtoResponse();
            segundaVisitaResponse.setId("visita-id-2");
            segundaVisitaResponse.setIdLocal(idLocal);

            when(buscarVisitaPorIdLocalUsecase.execute(idLocal))
                    .thenReturn(List.of(visitaDtoResponse, segundaVisitaResponse));

            ResponseEntity<List<VisitaDtoResponse>> resposta = visitaController.buscarVisitasPorIdLocal(idLocal);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNotNull(resposta.getBody()),
                    () -> assertEquals(2, resposta.getBody().size()),
                    () -> assertEquals("visita-id-1", resposta.getBody().get(0).getId()),
                    () -> assertEquals("visita-id-2", resposta.getBody().get(1).getId())
            );
            verify(buscarVisitaPorIdLocalUsecase).execute(idLocal);
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("deve retornar 200 OK com lista vazia quando não há visitas para o local")
        void deveRetornarListaVaziaQuandoNaoHaVisitas() {
            String idLocal = "local-sem-visitas";

            when(buscarVisitaPorIdLocalUsecase.execute(idLocal)).thenReturn(List.of());

            ResponseEntity<List<VisitaDtoResponse>> resposta = visitaController.buscarVisitasPorIdLocal(idLocal);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNotNull(resposta.getBody()),
                    () -> assertTrue(resposta.getBody().isEmpty())
            );
            verify(buscarVisitaPorIdLocalUsecase).execute(idLocal);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("POST /api/visita - adicionarVisita")
    class AdicionarVisita {

        @Test
        @DisplayName("deve converter o request para domínio, executar o use case e retornar 201 CREATED com o id")
        void deveAdicionarVisitaERetornarIdCom201() {
            when(mapper.toDomain(visitaDtoRequest)).thenReturn(visita);
            when(cadastrarVisitaUsecase.execute(visita)).thenReturn("visita-id-gerada");

            ResponseEntity<String> resposta = visitaController.adicionarVisita(visitaDtoRequest);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.CREATED, resposta.getStatusCode()),
                    () -> assertEquals("visita-id-gerada", resposta.getBody())
            );
            verify(mapper).toDomain(visitaDtoRequest);
            verify(cadastrarVisitaUsecase).execute(visita);
        }
    }

    @Nested
    @DisplayName("DELETE /api/visita/{id} - removerVisita")
    class RemoverVisita {

        @Test
        @DisplayName("deve executar o use case de remoção e retornar 200 OK sem body")
        void deveRemoverVisitaERetornar200SemBody() {
            String id = "visita-id-1";

            ResponseEntity<Void> resposta = visitaController.removerVisita(id);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNull(resposta.getBody())
            );
            verify(removerVisitaUsecase).execute(id);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("PUT /api/visita/{id} - atualizarVisita")
    class AtualizarVisita {

        @Test
        @DisplayName("deve converter o request, executar o use case de atualização com id e visita e retornar 200 OK sem body")
        void deveAtualizarVisitaERetornar200SemBody() {
            String id = "visita-id-1";

            when(mapper.toDomain(visitaDtoRequest)).thenReturn(visita);

            ResponseEntity<Void> resposta = visitaController.atualizarVisita(id, visitaDtoRequest);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNull(resposta.getBody())
            );
            verify(mapper).toDomain(visitaDtoRequest);
            verify(atualizarVisitaUsecase).execute(id, visita);
        }
    }
}
