package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.AutoCompleteResponse;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarLocalUsecaseImpl")
class BuscarLocalUsecaseImplTest {

    @Mock
    private LatitudeLongitudeInterfacePort latitudeLongitudeInterfacePort;

    @InjectMocks
    private BuscarLocalUsecaseImpl buscarLocalUsecaseImpl;

    @Nested
    @DisplayName("Quando a execução ocorre normalmente")
    class QuandoExecucaoNormal {

        @Test
        @DisplayName("deve retornar null quando o método é chamado com inputText e sessionToken válidos")
        void deveRetornarNullQuandoMetodoEChamadoComParametrosValidos() {
            AutoCompleteResponse resultado = buscarLocalUsecaseImpl.execute("restaurante", "token-abc-123");

            assertNull(resultado);
        }
    }
}
