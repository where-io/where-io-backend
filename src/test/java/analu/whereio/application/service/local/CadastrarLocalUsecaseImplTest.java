package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.ApiResponse;
import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Endereco;
import analu.whereio.application.model.Local;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CadastrarLocalUsecaseImpl")
class CadastrarLocalUsecaseImplTest {

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @Mock
    private LatitudeLongitudeInterfacePort latitudeLongitudePort;

    @Mock
    private SincronizarTagsDoLocalService sincronizarTagsDoLocalService;

    @InjectMocks
    private CadastrarLocalUsecaseImpl cadastrarLocalUsecaseImpl;

    private Local localSemCoordenadas;
    private Local localComCoordenadas;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = new Endereco();
        endereco.setLogradouro("Rua das Flores");
        endereco.setBairro("Centro");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01310-100");
        endereco.setPais("Brasil");

        localSemCoordenadas = new Local();
        localSemCoordenadas.setNome("Restaurante Bom Sabor");
        localSemCoordenadas.setEndereco(endereco);
        localSemCoordenadas.setCoordenadas(Coordenadas.builder().build());

        localComCoordenadas = new Local();
        localComCoordenadas.setNome("Café Central");
        localComCoordenadas.setEndereco(endereco);
        localComCoordenadas.setCoordenadas(Coordenadas.builder()
                .latitude("-23.5505")
                .longitude("-46.6333")
                .build());

        lenient().doNothing().when(sincronizarTagsDoLocalService).aplicar(any(Local.class));
    }

    private ApiResponse montarApiResponse(double latitude, double longitude) {
        ApiResponse response = new ApiResponse();
        ApiResponse.Result result = new ApiResponse.Result();
        ApiResponse.NavigationPoint navPoint = new ApiResponse.NavigationPoint();
        ApiResponse.Location location = new ApiResponse.Location();

        location.setLatitude(latitude);
        location.setLongitude(longitude);
        navPoint.setLocation(location);
        result.setNavigationPoints(List.of(navPoint));
        response.setResults(List.of(result));

        return response;
    }

    @Nested
    @DisplayName("Quando o nome do local já está cadastrado")
    class QuandoNomeJaExiste {

        @Test
        @DisplayName("deve lançar BusinessException com UNPROCESSABLE_ENTITY quando o nome já está cadastrado")
        void deveLancarBusinessExceptionQuandoNomeJaExiste() {
            when(localRepositoryPort.buscarPorNomeLocal(localSemCoordenadas.getNome()))
                    .thenReturn(localSemCoordenadas);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarLocalUsecaseImpl.execute(localSemCoordenadas)
            );

            assertAll(
                    () -> assertEquals("Local já foi cadastrado", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve não chamar cadastrarLocal quando o nome já existe")
        void naoDeveChamarCadastrarLocalQuandoNomeJaExiste() {
            when(localRepositoryPort.buscarPorNomeLocal(localSemCoordenadas.getNome()))
                    .thenReturn(localSemCoordenadas);

            assertThrows(
                    BusinessException.class,
                    () -> cadastrarLocalUsecaseImpl.execute(localSemCoordenadas)
            );

            verify(localRepositoryPort, never()).cadastrarLocal(any());
        }
    }

    @Nested
    @DisplayName("Quando as coordenadas já estão preenchidas")
    class QuandoCoordenadosJaPreenchidas {

        @Test
        @DisplayName("deve cadastrar o local sem chamar geocoding quando latitude e longitude já estão preenchidas")
        void deveCadastrarSemChamarGeocodingQuandoCoordenadosPreenchidas() throws IOException, InterruptedException {
            Local localSalvo = new Local();
            localSalvo.setId("id-gerado");
            localSalvo.setNome(localComCoordenadas.getNome());

            when(localRepositoryPort.buscarPorNomeLocal(localComCoordenadas.getNome())).thenReturn(null);
            when(localRepositoryPort.cadastrarLocal(localComCoordenadas)).thenReturn(localSalvo);

            Local resultado = cadastrarLocalUsecaseImpl.execute(localComCoordenadas);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals("id-gerado", resultado.getId())
            );
            verify(latitudeLongitudePort, never()).buscarLocalizacao(anyString());
            verify(localRepositoryPort).cadastrarLocal(localComCoordenadas);
            verify(sincronizarTagsDoLocalService).aplicar(localComCoordenadas);
        }
    }

    @Nested
    @DisplayName("Quando as coordenadas estão ausentes e o geocoding é chamado")
    class QuandoCoordenadosAusentes {

        @Test
        @DisplayName("deve buscar coordenadas via geocoding e cadastrar quando a latitude está nula")
        void deveBuscarGeocodingECadastrarQuandoLatitudeNula() throws IOException, InterruptedException {
            localSemCoordenadas.setCoordenadas(Coordenadas.builder().longitude("-46.6333").build());
            Local localSalvo = new Local();
            localSalvo.setId("id-gerado");
            ApiResponse apiResponse = montarApiResponse(-23.5505, -46.6333);

            when(localRepositoryPort.buscarPorNomeLocal(anyString())).thenReturn(null);
            when(latitudeLongitudePort.buscarLocalizacao(anyString())).thenReturn(apiResponse);
            when(localRepositoryPort.cadastrarLocal(any())).thenReturn(localSalvo);

            Local resultado = cadastrarLocalUsecaseImpl.execute(localSemCoordenadas);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(String.valueOf(-23.5505), localSemCoordenadas.getCoordenadas().getLatitude()),
                    () -> assertEquals(String.valueOf(-46.6333), localSemCoordenadas.getCoordenadas().getLongitude())
            );
            verify(latitudeLongitudePort).buscarLocalizacao(endereco.toString());
        }

        @Test
        @DisplayName("deve buscar coordenadas via geocoding e cadastrar quando a longitude está nula")
        void deveBuscarGeocodingECadastrarQuandoLongitudeNula() throws IOException, InterruptedException {
            localSemCoordenadas.setCoordenadas(Coordenadas.builder().latitude("-23.5505").build());
            Local localSalvo = new Local();
            localSalvo.setId("id-gerado");
            ApiResponse apiResponse = montarApiResponse(-23.5505, -46.6333);

            when(localRepositoryPort.buscarPorNomeLocal(anyString())).thenReturn(null);
            when(latitudeLongitudePort.buscarLocalizacao(anyString())).thenReturn(apiResponse);
            when(localRepositoryPort.cadastrarLocal(any())).thenReturn(localSalvo);

            Local resultado = cadastrarLocalUsecaseImpl.execute(localSemCoordenadas);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(String.valueOf(-23.5505), localSemCoordenadas.getCoordenadas().getLatitude()),
                    () -> assertEquals(String.valueOf(-46.6333), localSemCoordenadas.getCoordenadas().getLongitude())
            );
            verify(latitudeLongitudePort).buscarLocalizacao(endereco.toString());
        }

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando geocoding lança IOException")
        void deveLancarBusinessExceptionQuandoGeocodingLancaIOException() throws IOException, InterruptedException {
            when(localRepositoryPort.buscarPorNomeLocal(anyString())).thenReturn(null);
            when(latitudeLongitudePort.buscarLocalizacao(anyString()))
                    .thenThrow(new IOException("Erro de conexão"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarLocalUsecaseImpl.execute(localSemCoordenadas)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao cadastrar o local", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando geocoding lança InterruptedException")
        void deveLancarBusinessExceptionQuandoGeocodingLancaInterruptedException() throws IOException, InterruptedException {
            when(localRepositoryPort.buscarPorNomeLocal(anyString())).thenReturn(null);
            when(latitudeLongitudePort.buscarLocalizacao(anyString()))
                    .thenThrow(new InterruptedException("Thread interrompida"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarLocalUsecaseImpl.execute(localSemCoordenadas)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao cadastrar o local", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando geocoding lança RuntimeException")
        void deveLancarBusinessExceptionQuandoGeocodingLancaRuntimeException() throws IOException, InterruptedException {
            when(localRepositoryPort.buscarPorNomeLocal(anyString())).thenReturn(null);
            when(latitudeLongitudePort.buscarLocalizacao(anyString()))
                    .thenThrow(new RuntimeException("Erro inesperado"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarLocalUsecaseImpl.execute(localSemCoordenadas)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao cadastrar o local", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }
    }

    @Nested
    @DisplayName("Quando a persistência falha ao cadastrar")
    class QuandoPersistenciaFalha {

        @Test
        @DisplayName("deve lançar BusinessException com INTERNAL_SERVER_ERROR quando cadastrarLocal lança Exception")
        void deveLancarBusinessExceptionQuandoCadastrarLocalFalha() {
            when(localRepositoryPort.buscarPorNomeLocal(localComCoordenadas.getNome())).thenReturn(null);
            when(localRepositoryPort.cadastrarLocal(localComCoordenadas))
                    .thenThrow(new RuntimeException("Falha no banco de dados"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> cadastrarLocalUsecaseImpl.execute(localComCoordenadas)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao cadastrar o local", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
