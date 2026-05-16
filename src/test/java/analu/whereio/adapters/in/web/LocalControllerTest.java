package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.LocalConverter;
import analu.whereio.adapters.in.web.dto.request.LocalBuscarDtoRequest;
import analu.whereio.adapters.in.web.dto.request.LocalDtoRequest;
import analu.whereio.adapters.in.web.dto.response.LocalBuscarDtoResponse;
import analu.whereio.adapters.in.web.dto.response.LocalDtoResponse;
import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Endereco;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.in.local.AtualizarLocalUsecase;
import analu.whereio.application.ports.in.local.BuscarDetalhesPlaceUsecase;
import analu.whereio.application.ports.in.local.BuscarLocalUsecase;
import analu.whereio.application.ports.in.local.BuscarTodosLocalUsecase;
import analu.whereio.application.ports.in.local.CadastrarLocalUsecase;
import analu.whereio.application.ports.in.local.RemoverLocalUsecase;
import analu.whereio.config.security.JwtUserPrincipal;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocalController")
class LocalControllerTest {

    private static final JwtUserPrincipal PRINCIPAL = JwtUserPrincipal.testPrincipal("user-1");

    @Mock
    private CadastrarLocalUsecase cadastrarLocalUsecase;

    @Mock
    private AtualizarLocalUsecase atualizarLocalUsecase;

    @Mock
    private BuscarLocalUsecase buscarLocalUsecase;

    @Mock
    private BuscarDetalhesPlaceUsecase buscarDetalhesPlaceUsecase;

    @Mock
    private BuscarTodosLocalUsecase buscarTodosLocalUsecase;

    @Mock
    private RemoverLocalUsecase removerLocalUsecase;

    @Mock
    private LocalConverter mapper;

    @InjectMocks
    private LocalController localController;

    private Local local;
    private LocalDtoRequest localDtoRequest;
    private LocalDtoResponse localDtoResponse;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua das Flores");
        endereco.setBairro("Centro");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01310-100");
        endereco.setPais("Brasil");

        Coordenadas coordenadas = Coordenadas.builder()
                .latitude("-23.5505")
                .longitude("-46.6333")
                .build();

        local = new Local();
        local.setId("local-id-1");
        local.setNome("Restaurante Bom Sabor");
        local.setEndereco(endereco);
        local.setCoordenadas(coordenadas);

        localDtoRequest = LocalDtoRequest.builder()
                .nome("Restaurante Bom Sabor")
                .endereco(endereco)
                .coordenadas(coordenadas)
                .build();

        localDtoResponse = new LocalDtoResponse();
        localDtoResponse.setId("local-id-1");
        localDtoResponse.setNome("Restaurante Bom Sabor");
        localDtoResponse.setEndereco(endereco);
        localDtoResponse.setCoordenadas(coordenadas);
    }

    @Nested
    @DisplayName("POST /api/local - cadastrarLocal")
    class CadastrarLocal {

        @Test
        @DisplayName("deve converter o request para domínio, executar o use case, mapear a resposta e retornar 200 OK com o id")
        void deveCadastrarLocalERetornarIdCom200() {
            when(mapper.toDomain(localDtoRequest)).thenReturn(local);
            when(cadastrarLocalUsecase.execute(local)).thenReturn(local);
            when(mapper.toResponse(local)).thenReturn(localDtoResponse);

            ResponseEntity<String> resposta = localController.cadastrarLocal(PRINCIPAL, localDtoRequest);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals("local-id-1", resposta.getBody()),
                    () -> assertEquals("user-1", local.getOwnerUserId())
            );
            verify(mapper).toDomain(localDtoRequest);
            verify(cadastrarLocalUsecase).execute(local);
            verify(mapper).toResponse(local);
        }
    }

    @Nested
    @DisplayName("DELETE /api/local/{id} - deletarLocal")
    class DeletarLocal {

        @Test
        @DisplayName("deve executar o use case de remoção e retornar 200 OK sem body")
        void deveDeletarLocalERetornar200SemBody() {
            String id = "local-id-1";

            ResponseEntity<Void> resposta = localController.deletarLocal(PRINCIPAL, id);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNull(resposta.getBody())
            );
            verify(removerLocalUsecase).execute(id, "user-1");
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("POST /api/local/buscar-local - buscarLocal")
    class BuscarLocal {

        @Test
        @DisplayName("deve executar o use case de busca com inputText e sessionToken e retornar 200 OK com o response mapeado")
        void deveBuscarLocalERetornarResponseCom200() {
            LocalBuscarDtoRequest buscarRequest = new LocalBuscarDtoRequest();
            buscarRequest.setInputText("Restaurante");
            buscarRequest.setSessionToken("token-123");

            AutoCompleteResponse autoCompleteResponse = new AutoCompleteResponse(List.of());
            LocalBuscarDtoResponse buscarDtoResponse = new LocalBuscarDtoResponse();

            when(buscarLocalUsecase.execute("Restaurante", "token-123")).thenReturn(autoCompleteResponse);
            when(mapper.toBuscarResponse(autoCompleteResponse)).thenReturn(buscarDtoResponse);

            ResponseEntity<LocalBuscarDtoResponse> resposta = localController.buscarLocal(buscarRequest);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertSame(buscarDtoResponse, resposta.getBody())
            );
            verify(buscarLocalUsecase).execute("Restaurante", "token-123");
            verify(mapper).toBuscarResponse(autoCompleteResponse);
        }
    }

    @Nested
    @DisplayName("GET /api/local/all - buscarTodosLocais")
    class BuscarTodosLocais {

        @Test
        @DisplayName("deve executar o use case, mapear cada local e retornar 200 OK com a lista de responses")
        void deveBuscarTodosLocaisERetornarListaCom200() {
            Local segundoLocal = new Local();
            segundoLocal.setId("local-id-2");
            segundoLocal.setNome("Bar do João");

            LocalDtoResponse segundoResponse = new LocalDtoResponse();
            segundoResponse.setId("local-id-2");
            segundoResponse.setNome("Bar do João");

            when(buscarTodosLocalUsecase.execute("user-1")).thenReturn(List.of(local, segundoLocal));
            when(mapper.toResponse(local)).thenReturn(localDtoResponse);
            when(mapper.toResponse(segundoLocal)).thenReturn(segundoResponse);

            ResponseEntity<List<LocalDtoResponse>> resposta = localController.buscarTodosLocais(PRINCIPAL);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNotNull(resposta.getBody()),
                    () -> assertEquals(2, resposta.getBody().size()),
                    () -> assertEquals("local-id-1", resposta.getBody().get(0).getId()),
                    () -> assertEquals("local-id-2", resposta.getBody().get(1).getId())
            );
            verify(buscarTodosLocalUsecase).execute("user-1");
            verify(mapper).toResponse(local);
            verify(mapper).toResponse(segundoLocal);
        }

        @Test
        @DisplayName("deve retornar 200 OK com lista vazia quando não há locais cadastrados")
        void deveRetornarListaVaziaQuandoNaoHaLocais() {
            when(buscarTodosLocalUsecase.execute("user-1")).thenReturn(List.of());

            ResponseEntity<List<LocalDtoResponse>> resposta = localController.buscarTodosLocais(PRINCIPAL);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNotNull(resposta.getBody()),
                    () -> assertTrue(resposta.getBody().isEmpty())
            );
            verify(buscarTodosLocalUsecase).execute("user-1");
            verifyNoMoreInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("PUT /api/local/{id} - atualizarLocal")
    class AtualizarLocal {

        @Test
        @DisplayName("deve converter o request, executar o use case de atualização e retornar 200 OK sem body")
        void deveAtualizarLocalERetornar200SemBody() {
            String id = "local-id-1";

            when(mapper.toDomain(localDtoRequest)).thenReturn(local);

            ResponseEntity<LocalDtoResponse> resposta = localController.atualizarLocal(PRINCIPAL, id, localDtoRequest);

            assertAll(
                    () -> assertNotNull(resposta),
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertNull(resposta.getBody())
            );
            verify(mapper).toDomain(localDtoRequest);
            verify(atualizarLocalUsecase).execute(local, id, "user-1");
        }
    }
}
