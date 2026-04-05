package analu.whereio.application.service.local;

import analu.whereio.adapters.out.external.geocoding.record.LatitudeLongitudeRecord;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarLocalUsecaseImpl")
class AtualizarLocalUsecaseImplTest {

    @Mock
    private LocalRepositoryPort localRepositoryPort;

    @Mock
    private LatitudeLongitudeInterfacePort latitudeLongitudePort;

    @InjectMocks
    private AtualizarLocalUsecaseImpl atualizarLocalUsecaseImpl;

    private Local localExistente;
    private Local localParaAtualizar;
    private Endereco endereco;
    private static final String ID_VALIDO = "abc-123";

    @BeforeEach
    void setUp() {
        endereco = new Endereco();
        endereco.setLogradouro("Rua das Flores");
        endereco.setBairro("Centro");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01310-100");
        endereco.setPais("Brasil");

        localExistente = new Local();
        localExistente.setId(ID_VALIDO);
        localExistente.setNome("Restaurante Bom Sabor");

        localParaAtualizar = new Local();
        localParaAtualizar.setNome("Restaurante Bom Sabor Atualizado");
        localParaAtualizar.setEndereco(endereco);
    }

    @Nested
    @DisplayName("Quando o ID não existe no repositório")
    class QuandoIdNaoExiste {

        @Test
        @DisplayName("deve lançar BusinessException com status NOT_FOUND quando o ID não está cadastrado")
        void deveLancarBusinessExceptionQuandoIdNaoExiste() {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(null);

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO)
            );

            assertAll(
                    () -> assertEquals("ID de local não existe", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve não chamar atualizarLocal quando o ID não existe")
        void naoDeveAtualizarLocalQuandoIdNaoExiste() {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(null);

            assertThrows(
                    BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO)
            );

            verify(localRepositoryPort, never()).atualizarLocal(any());
        }
    }

    @Nested
    @DisplayName("Quando o fluxo completo é executado com sucesso")
    class QuandoSucesso {

        @Test
        @DisplayName("deve atualizar o local com coordenadas e id corretos quando todos os dados são válidos")
        void deveAtualizarLocalComSucessoQuandoDadosValidos() throws IOException, InterruptedException {
            LatitudeLongitudeRecord coordenadasRecord = new LatitudeLongitudeRecord("-23.5505", "-46.6333");

            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(latitudeLongitudePort.ConverterEnderecoParaCoordenadas(anyString()))
                    .thenReturn(coordenadasRecord);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO);

            assertAll(
                    () -> assertEquals(ID_VALIDO, localParaAtualizar.getId()),
                    () -> assertEquals("-23.5505", localParaAtualizar.getCoordenadas().getLatitude()),
                    () -> assertEquals("-46.6333", localParaAtualizar.getCoordenadas().getLongitude())
            );
        }

        @Test
        @DisplayName("deve chamar atualizarLocal com o local populado quando todos os dados são válidos")
        void deveChamarAtualizarLocalComLocalCorretoQuandoDadosValidos() throws IOException, InterruptedException {
            LatitudeLongitudeRecord coordenadasRecord = new LatitudeLongitudeRecord("-23.5505", "-46.6333");

            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(latitudeLongitudePort.ConverterEnderecoParaCoordenadas(anyString()))
                    .thenReturn(coordenadasRecord);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO);

            verify(localRepositoryPort).atualizarLocal(eq(localParaAtualizar));
        }

        @Test
        @DisplayName("deve chamar ConverterEnderecoParaCoordenadas com o toString do endereço")
        void deveChamarConversaoComEnderecoFormatadoQuandoDadosValidos() throws IOException, InterruptedException {
            LatitudeLongitudeRecord coordenadasRecord = new LatitudeLongitudeRecord("-23.5505", "-46.6333");
            String enderecoFormatado = endereco.toString();

            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(latitudeLongitudePort.ConverterEnderecoParaCoordenadas(enderecoFormatado))
                    .thenReturn(coordenadasRecord);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO);

            verify(latitudeLongitudePort).ConverterEnderecoParaCoordenadas(enderecoFormatado);
        }
    }

    @Nested
    @DisplayName("Quando o serviço de geocoding falha")
    class QuandoGeocodingFalha {

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando geocoding lança IOException")
        void deveLancarBusinessExceptionQuandoGeocodingLancaIOException() throws IOException, InterruptedException {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(latitudeLongitudePort.ConverterEnderecoParaCoordenadas(anyString()))
                    .thenThrow(new IOException("Erro de conexão"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO)
            );

            assertAll(
                    () -> assertEquals("Local não foi encontrado", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando geocoding lança InterruptedException")
        void deveLancarBusinessExceptionQuandoGeocodingLancaInterruptedException() throws IOException, InterruptedException {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(latitudeLongitudePort.ConverterEnderecoParaCoordenadas(anyString()))
                    .thenThrow(new InterruptedException("Thread interrompida"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO)
            );

            assertAll(
                    () -> assertEquals("Local não foi encontrado", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve lançar BusinessException com NOT_FOUND quando geocoding lança RuntimeException")
        void deveLancarBusinessExceptionQuandoGeocodingLancaRuntimeException() throws IOException, InterruptedException {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(latitudeLongitudePort.ConverterEnderecoParaCoordenadas(anyString()))
                    .thenThrow(new RuntimeException("Erro inesperado"));

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO)
            );

            assertAll(
                    () -> assertEquals("Local não foi encontrado", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus())
            );
        }

        @Test
        @DisplayName("deve não chamar atualizarLocal quando geocoding falha")
        void naoDeveAtualizarLocalQuandoGeocodingFalha() throws IOException, InterruptedException {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(latitudeLongitudePort.ConverterEnderecoParaCoordenadas(anyString()))
                    .thenThrow(new IOException("Erro de conexão"));

            assertThrows(
                    BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO)
            );

            verify(localRepositoryPort, never()).atualizarLocal(any());
        }
    }

    @Nested
    @DisplayName("Quando a persistência falha ao atualizar")
    class QuandoPersistenciaFalha {

        @Test
        @DisplayName("deve lançar BusinessException com INTERNAL_SERVER_ERROR quando atualizarLocal lança Exception")
        void deveLancarBusinessExceptionQuandoPersistenciaFalha() throws IOException, InterruptedException {
            LatitudeLongitudeRecord coordenadasRecord = new LatitudeLongitudeRecord("-23.5505", "-46.6333");

            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(latitudeLongitudePort.ConverterEnderecoParaCoordenadas(anyString()))
                    .thenReturn(coordenadasRecord);
            doThrow(new RuntimeException("Falha no banco de dados"))
                    .when(localRepositoryPort).atualizarLocal(any());

            BusinessException excecao = assertThrows(
                    BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO)
            );

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao atualizar o local", excecao.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, excecao.getStatus())
            );
        }
    }
}
