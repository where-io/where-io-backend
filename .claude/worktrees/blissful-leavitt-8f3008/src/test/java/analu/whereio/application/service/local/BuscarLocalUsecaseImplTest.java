package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.adapters.out.external.geocoding.record.Suggestion;
import analu.whereio.application.ports.out.PlacesApiPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarLocalUsecaseImpl")
class BuscarLocalUsecaseImplTest {

    @Mock
    private PlacesApiPort placesApiPort;

    private BuscarLocalUsecaseImpl buscarLocalUsecaseImpl;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        buscarLocalUsecaseImpl = new BuscarLocalUsecaseImpl(placesApiPort);
    }

    @Nested
    @DisplayName("Quando input tem no máximo 3 caracteres")
    class InputCurto {

        @Test
        @DisplayName("deve retornar lista vazia sem chamar Places")
        void deveRetornarListaVaziaSemChamarPlaces() {
            AutoCompleteResponse resultado = buscarLocalUsecaseImpl.execute("ab", "token");

            assertNotNull(resultado);
            assertTrue(resultado.suggestions().isEmpty());
            verifyNoInteractions(placesApiPort);
        }
    }

    @Nested
    @DisplayName("Quando a execução ocorre normalmente")
    class QuandoExecucaoNormal {

        @Test
        @DisplayName("deve delegar ao PlacesApiPort e retornar sugestões")
        void deveDelegarAoPort() {
            AutoCompleteResponse esperado = new AutoCompleteResponse(List.of(
                    new Suggestion("p1", "Rua X", "Rua X", "Cidade")));
            when(placesApiPort.placeAutocomplete("restaurante", "tok")).thenReturn(esperado);

            AutoCompleteResponse resultado = buscarLocalUsecaseImpl.execute("restaurante", "tok");

            assertEquals(1, resultado.suggestions().size());
            assertEquals("p1", resultado.suggestions().get(0).placeId());
        }
    }

    @Nested
    @DisplayName("Quando PlacesApiPort lança exceção")
    class QuandoFalha {

        @Test
        @DisplayName("deve retornar lista vazia")
        void deveRetornarListaVazia() {
            when(placesApiPort.placeAutocomplete(anyString(), anyString()))
                    .thenThrow(new RuntimeException("erro"));

            AutoCompleteResponse resultado = buscarLocalUsecaseImpl.execute("centro", "t");

            assertTrue(resultado.suggestions().isEmpty());
        }
    }
}
