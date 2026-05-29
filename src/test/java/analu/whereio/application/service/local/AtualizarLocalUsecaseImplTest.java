package analu.whereio.application.service.local;

import analu.whereio.application.model.Categoria;
import analu.whereio.application.model.Coordenadas;
import analu.whereio.application.model.Endereco;
import analu.whereio.application.model.Local;
import analu.whereio.application.model.Visita;
import analu.whereio.application.ports.out.LatitudeLongitudeInterfacePort;
import analu.whereio.application.ports.out.LocalRepositoryPort;
import analu.whereio.application.ports.out.VisitaRepositoryPort;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarLocalUsecaseImpl")
class AtualizarLocalUsecaseImplTest {

    @Mock private LocalRepositoryPort localRepositoryPort;
    @Mock private LatitudeLongitudeInterfacePort latitudeLongitudePort;
    @Mock private VisitaRepositoryPort visitaRepositoryPort;
    @Mock private SincronizarTagsDoLocalService sincronizarTagsDoLocalService;

    @InjectMocks
    private AtualizarLocalUsecaseImpl atualizarLocalUsecaseImpl;

    private Local localExistente;
    private Local localParaAtualizar;
    private Endereco endereco;
    private static final String ID_VALIDO = "abc-123";
    private static final String OWNER_ID = "owner-1";

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
        localExistente.setOwnerUserId(OWNER_ID);

        localParaAtualizar = new Local();
        localParaAtualizar.setNome("Restaurante Bom Sabor Atualizado");

        lenient().doNothing().when(sincronizarTagsDoLocalService).aplicar(any(Local.class));
    }

    @Nested
    @DisplayName("Quando o local não é encontrado ou owner não confere")
    class QuandoLocalNaoEncontrado {

        @Test
        @DisplayName("deve lançar NOT_FOUND quando o ID não está cadastrado")
        void deveLancarNotFoundQuandoIdNaoExiste() {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID));

            assertAll(
                    () -> assertEquals("Local nao encontrado para atualizacao", ex.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, ex.getStatus())
            );
        }

        @Test
        @DisplayName("deve lançar NOT_FOUND quando ownerUserId não confere")
        void deveLancarNotFoundQuandoOwnerMismatch() {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, "outro-owner"));

            assertAll(
                    () -> assertEquals("Local nao encontrado para atualizacao", ex.getMessage()),
                    () -> assertEquals(HttpStatus.NOT_FOUND, ex.getStatus())
            );
        }

        @Test
        @DisplayName("não deve chamar atualizarLocal quando local não encontrado")
        void naoDeveAtualizarLocalQuandoIdNaoExiste() {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(null);

            assertThrows(BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID));

            verify(localRepositoryPort, never()).atualizarLocal(any());
        }
    }

    @Nested
    @DisplayName("Quando o fluxo completo é executado com sucesso")
    class QuandoSucesso {

        @Test
        @DisplayName("deve setar o id e chamar atualizarLocal quando dados válidos")
        void deveSetarIdEChamarAtualizarLocal() {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertEquals(ID_VALIDO, localParaAtualizar.getId());
            verify(localRepositoryPort).atualizarLocal(localParaAtualizar);
        }

        @Test
        @DisplayName("deve sobrescrever ownerUserId do request com o do local existente para prevenir escalação de privilégio")
        void devePreservarOwnerUserIdDoLocalExistente() {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            localParaAtualizar.setOwnerUserId("atacante-id");

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertEquals(OWNER_ID, localParaAtualizar.getOwnerUserId());
        }

        @Test
        @DisplayName("deve preservar visitacao do local existente quando request não envia o campo (null)")
        void devePreservarVisitacaoQuandoNullNoRequest() {
            localExistente.setVisitacao(false);
            localParaAtualizar.setVisitacao(null);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertEquals(false, localParaAtualizar.getVisitacao());
        }

        @Test
        @DisplayName("deve preservar fotos já salvas quando o payload traz lista vazia")
        void devePreservarFotosQuandoPayloadComListaVazia() {
            localExistente.setFotos(new ArrayList<>(List.of("a.png", "b.png")));
            localParaAtualizar.setFotos(new ArrayList<>());
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertEquals(List.of("a.png", "b.png"), localParaAtualizar.getFotos());
        }

        @Test
        @DisplayName("deve preservar endereco existente quando update DTO não envia endereco (null)")
        void devePreservarEnderecoQuandoNullNoRequest() {
            localExistente.setEndereco(endereco);
            localParaAtualizar.setEndereco(null);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertSame(endereco, localParaAtualizar.getEndereco());
        }

        @Test
        @DisplayName("deve preservar coordenadas existentes quando update DTO não envia coordenadas (null)")
        void devePreservarCoordenadasQuandoNullNoRequest() {
            Coordenadas salvas = new Coordenadas();
            salvas.setLatitude("-23.5");
            salvas.setLongitude("-46.7");
            localExistente.setCoordenadas(salvas);
            localParaAtualizar.setCoordenadas(null);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertSame(salvas, localParaAtualizar.getCoordenadas());
        }

        @Test
        @DisplayName("deve usar coordenadas enviadas no request quando presentes (não sobrescreve com existente)")
        void deveUsarCoordenadasDoRequestQuandoPresentes() {
            Coordenadas salvas = new Coordenadas();
            salvas.setLatitude("-23.5");
            salvas.setLongitude("-46.7");
            localExistente.setCoordenadas(salvas);

            Coordenadas novas = new Coordenadas();
            novas.setLatitude("-22.9");
            novas.setLongitude("-43.1");
            localParaAtualizar.setCoordenadas(novas);

            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertEquals("-22.9", localParaAtualizar.getCoordenadas().getLatitude());
            assertEquals("-43.1", localParaAtualizar.getCoordenadas().getLongitude());
//            verify(latitudeLongitudePort, never()).ConverterEnderecoParaCoordenadas(any());
        }

        @Test
        @DisplayName("deve preservar imagemUrl existente quando request não envia o campo (null)")
        void devePreservarImagemUrlQuandoNullNoRequest() {
            localExistente.setImagemUrl("https://storage/foto.png");
            localParaAtualizar.setImagemUrl(null);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertEquals("https://storage/foto.png", localParaAtualizar.getImagemUrl());
        }

        @Test
        @DisplayName("deve remover imagemUrl quando request envia string vazia")
        void deveRemoverImagemUrlQuandoStringVazia() {
            localExistente.setImagemUrl("https://storage/foto.png");
            localParaAtualizar.setImagemUrl("");
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertNull(localParaAtualizar.getImagemUrl());
        }
    }

    @Nested
    @DisplayName("Comportamento de tags na atualização")
    class QuandoTags {

        @Test
        @DisplayName("deve preservar idTags existentes quando update DTO não envia tags")
        void devePreservarIdTagsQuandoTagsNaoEnviadas() {
            localExistente.setIdTags(new ArrayList<>(List.of("tag-1", "tag-2")));
            localParaAtualizar.setIdTags(null);
            localParaAtualizar.setTags(null);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            assertEquals(List.of("tag-1", "tag-2"), localParaAtualizar.getIdTags());
            verify(sincronizarTagsDoLocalService, never()).aplicar(any());
        }

        @Test
        @DisplayName("deve chamar sincronizarTags quando idTags enviadas no request")
        void deveSincronizarTagsQuandoIdTagsEnviadas() {
            localParaAtualizar.setIdTags(new ArrayList<>(List.of("tag-1")));
            localParaAtualizar.setTags(null);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            verify(sincronizarTagsDoLocalService).aplicar(localParaAtualizar);
        }

        @Test
        @DisplayName("deve chamar sincronizarTags quando tags (Categoria) enviadas no request")
        void deveSincronizarTagsQuandoCategoriaEnviada() {
            Categoria cat = new Categoria();
            cat.setNome("Japonês");
            localParaAtualizar.setIdTags(null);
            localParaAtualizar.setTags(new ArrayList<>(List.of(cat)));
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            verify(sincronizarTagsDoLocalService).aplicar(localParaAtualizar);
        }
    }

    @Nested
    @DisplayName("Quando visitacao é false e o local já tem visitas")
    class QuandoVisitacaoFalseComVisitas {

        @Test
        @DisplayName("deve lançar UNPROCESSABLE_ENTITY quando tentar desabilitar visitacao em local com visitas")
        void deveLancarBusinessExceptionQuandoVisitacaoFalseComVisitas() {
            localParaAtualizar.setVisitacao(false);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(visitaRepositoryPort.buscarVisitasPorIdLocal(ID_VALIDO, OWNER_ID))
                    .thenReturn(List.of(new Visita()));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID));

            assertAll(
                    () -> assertEquals("Local com visitas não pode ter a visitação desabilitada", ex.getMessage()),
                    () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus())
            );
        }

        @Test
        @DisplayName("não deve chamar atualizarLocal quando visitacao false com visitas existentes")
        void naoDeveAtualizarLocalQuandoVisitacaoFalseComVisitas() {
            localParaAtualizar.setVisitacao(false);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(visitaRepositoryPort.buscarVisitasPorIdLocal(ID_VALIDO, OWNER_ID))
                    .thenReturn(List.of(new Visita()));

            assertThrows(BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID));

            verify(localRepositoryPort, never()).atualizarLocal(any());
        }

        @Test
        @DisplayName("deve permitir desabilitar visitacao quando local não tem visitas")
        void devePermitirDesabilitarVisitacaoQuandoSemVisitas() {
            localParaAtualizar.setVisitacao(false);
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            when(visitaRepositoryPort.buscarVisitasPorIdLocal(ID_VALIDO, OWNER_ID)).thenReturn(List.of());

            atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID);

            verify(localRepositoryPort).atualizarLocal(localParaAtualizar);
            assertEquals(false, localParaAtualizar.getVisitacao());
        }
    }

    @Nested
    @DisplayName("Quando a persistência falha ao atualizar")
    class QuandoPersistenciaFalha {

        @Test
        @DisplayName("deve lançar INTERNAL_SERVER_ERROR quando atualizarLocal lança Exception")
        void deveLancarBusinessExceptionQuandoPersistenciaFalha() {
            when(localRepositoryPort.buscarPorIdLocal(ID_VALIDO)).thenReturn(localExistente);
            doThrow(new RuntimeException("Falha no banco de dados"))
                    .when(localRepositoryPort).atualizarLocal(any());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> atualizarLocalUsecaseImpl.execute(localParaAtualizar, ID_VALIDO, OWNER_ID));

            assertAll(
                    () -> assertEquals("Ocorreu um erro ao atualizar o local", ex.getMessage()),
                    () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus())
            );
        }
    }
}
