package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.AmigosConverter;
import analu.whereio.adapters.in.web.dto.request.EnviarConviteAmizadeRequest;
import analu.whereio.adapters.in.web.dto.response.AmigoPerfilResponse;
import analu.whereio.adapters.in.web.dto.response.AmizadeConviteResponse;
import analu.whereio.adapters.in.web.dto.response.EnviarConviteAmizadeResponse;
import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.amigos.AceitarConviteAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.EnviarConviteAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.ListarAmigosUsecase;
import analu.whereio.application.ports.in.amigos.ListarConvitesEnviadosAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.ListarConvitesRecebidosAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.RecusarOuCancelarConviteAmizadeUsecase;
import analu.whereio.application.ports.in.amigos.RemoverAmigoUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
import analu.whereio.config.security.JwtUserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AmigosController")
class AmigosControllerTest {

    private static final JwtUserPrincipal PRINCIPAL = JwtUserPrincipal.testPrincipal("user-1");

    @Mock
    private EnviarConviteAmizadeUsecase enviarConviteAmizadeUsecase;

    @Mock
    private AceitarConviteAmizadeUsecase aceitarConviteAmizadeUsecase;

    @Mock
    private ListarConvitesRecebidosAmizadeUsecase listarConvitesRecebidosAmizadeUsecase;

    @Mock
    private ListarConvitesEnviadosAmizadeUsecase listarConvitesEnviadosAmizadeUsecase;

    @Mock
    private ListarAmigosUsecase listarAmigosUsecase;

    @Mock
    private RecusarOuCancelarConviteAmizadeUsecase recusarOuCancelarConviteAmizadeUsecase;

    @Mock
    private RemoverAmigoUsecase removerAmigoUsecase;

    @Mock
    private AmigosConverter converter;

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private AmigosController amigosController;

    @Nested
    @DisplayName("GET /api/amigos - listarAmigos")
    class ListarAmigos {

        @Test
        @DisplayName("deve retornar lista com fotoPerfilUrl preenchida quando usuário tem foto")
        void devePreencherFotoPerfilUrlQuandoTemFoto() {
            UserAccount amigo = new UserAccount();
            amigo.setId("amigo-1");
            amigo.setNomeUsuario("joao123");
            amigo.setNome("João");
            amigo.setFotoPerfil("foto_key.jpg");

            AmigoPerfilResponse resp = new AmigoPerfilResponse();
            resp.setId("amigo-1");
            resp.setNomeUsuario("joao123");
            resp.setNome("João");

            when(listarAmigosUsecase.execute("user-1")).thenReturn(List.of(amigo));
            when(converter.toAmigoResponse(amigo)).thenReturn(resp);
            when(fileStoragePort.gerarUrlAssinada("foto_key.jpg")).thenReturn("/media/foto_key.jpg");

            ResponseEntity<List<AmigoPerfilResponse>> resposta = amigosController.listarAmigos(PRINCIPAL);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals(1, resposta.getBody().size()),
                    () -> assertEquals("/media/foto_key.jpg", resposta.getBody().get(0).getFotoPerfilUrl()),
                    () -> assertEquals("joao123", resposta.getBody().get(0).getNomeUsuario())
            );
            verify(fileStoragePort).gerarUrlAssinada("foto_key.jpg");
        }

        @Test
        @DisplayName("deve retornar lista com fotoPerfilUrl nula quando usuário não tem foto")
        void deveDeixarFotoPerfilUrlNulaQuandoSemFoto() {
            UserAccount amigo = new UserAccount();
            amigo.setId("amigo-2");
            amigo.setNomeUsuario("maria99");
            amigo.setNome("Maria");
            amigo.setFotoPerfil(null);

            AmigoPerfilResponse resp = new AmigoPerfilResponse();
            resp.setId("amigo-2");
            resp.setNomeUsuario("maria99");
            resp.setNome("Maria");

            when(listarAmigosUsecase.execute("user-1")).thenReturn(List.of(amigo));
            when(converter.toAmigoResponse(amigo)).thenReturn(resp);

            ResponseEntity<List<AmigoPerfilResponse>> resposta = amigosController.listarAmigos(PRINCIPAL);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals(1, resposta.getBody().size()),
                    () -> assertNull(resposta.getBody().get(0).getFotoPerfilUrl())
            );
            verify(fileStoragePort, never()).gerarUrlAssinada(any());
        }

        @Test
        @DisplayName("deve retornar lista vazia quando usuário não tem amigos")
        void deveRetornarListaVaziaQuandoSemAmigos() {
            when(listarAmigosUsecase.execute("user-1")).thenReturn(List.of());

            ResponseEntity<List<AmigoPerfilResponse>> resposta = amigosController.listarAmigos(PRINCIPAL);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertTrue(resposta.getBody().isEmpty())
            );
            verifyNoInteractions(fileStoragePort, converter);
        }

        @Test
        @DisplayName("deve enriquecer múltiplos amigos com URLs assinadas corretamente")
        void deveEnriquecerMultiplosAmigosComUrlsAssinadas() {
            UserAccount amigo1 = new UserAccount();
            amigo1.setId("amigo-1");
            amigo1.setNomeUsuario("joao123");
            amigo1.setFotoPerfil("foto1.jpg");

            UserAccount amigo2 = new UserAccount();
            amigo2.setId("amigo-2");
            amigo2.setNomeUsuario("maria99");
            amigo2.setFotoPerfil(null);

            UserAccount amigo3 = new UserAccount();
            amigo3.setId("amigo-3");
            amigo3.setNomeUsuario("pedro456");
            amigo3.setFotoPerfil("foto3.jpg");

            AmigoPerfilResponse resp1 = new AmigoPerfilResponse();
            resp1.setId("amigo-1");
            resp1.setNomeUsuario("joao123");

            AmigoPerfilResponse resp2 = new AmigoPerfilResponse();
            resp2.setId("amigo-2");
            resp2.setNomeUsuario("maria99");

            AmigoPerfilResponse resp3 = new AmigoPerfilResponse();
            resp3.setId("amigo-3");
            resp3.setNomeUsuario("pedro456");

            when(listarAmigosUsecase.execute("user-1")).thenReturn(List.of(amigo1, amigo2, amigo3));
            when(converter.toAmigoResponse(amigo1)).thenReturn(resp1);
            when(converter.toAmigoResponse(amigo2)).thenReturn(resp2);
            when(converter.toAmigoResponse(amigo3)).thenReturn(resp3);
            when(fileStoragePort.gerarUrlAssinada("foto1.jpg")).thenReturn("/media/foto1.jpg");
            when(fileStoragePort.gerarUrlAssinada("foto3.jpg")).thenReturn("/media/foto3.jpg");

            ResponseEntity<List<AmigoPerfilResponse>> resposta = amigosController.listarAmigos(PRINCIPAL);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals(3, resposta.getBody().size()),
                    () -> assertEquals("/media/foto1.jpg", resposta.getBody().get(0).getFotoPerfilUrl()),
                    () -> assertNull(resposta.getBody().get(1).getFotoPerfilUrl()),
                    () -> assertEquals("/media/foto3.jpg", resposta.getBody().get(2).getFotoPerfilUrl())
            );
            verify(fileStoragePort, times(2)).gerarUrlAssinada(any());
        }
    }

    @Nested
    @DisplayName("POST /api/amigos/convites - enviarConvite")
    class EnviarConvite {

        @Test
        @DisplayName("deve enviar convite e retornar 201 CREATED")
        void deveEnviarConviteERetornar201() {
            EnviarConviteAmizadeRequest req = new EnviarConviteAmizadeRequest();
            req.setNomeUsuarioDestino("joao123");

            Friendship friendship = new Friendship();
            friendship.setId("friendship-1");
            friendship.setRequesterUserId("user-1");
            friendship.setAddresseeUserId("amigo-1");
            friendship.setStatus(FriendshipStatus.PENDING);
            friendship.setCreatedAt(Instant.now());

            AmizadeConviteResponse conviteResponse = new AmizadeConviteResponse();
            conviteResponse.setId("friendship-1");

            when(enviarConviteAmizadeUsecase.execute("user-1", "joao123")).thenReturn(friendship);
            when(converter.toConviteResponse(friendship)).thenReturn(conviteResponse);

            ResponseEntity<EnviarConviteAmizadeResponse> resposta = amigosController.enviarConvite(PRINCIPAL, req);

            assertAll(
                    () -> assertEquals(HttpStatus.CREATED, resposta.getStatusCode()),
                    () -> assertTrue(resposta.getBody().isSucesso()),
                    () -> assertEquals("Convite enviado com sucesso.", resposta.getBody().getMensagem()),
                    () -> assertNotNull(resposta.getBody().getConvite())
            );
            verify(enviarConviteAmizadeUsecase).execute("user-1", "joao123");
            verify(converter).toConviteResponse(friendship);
        }
    }
}
