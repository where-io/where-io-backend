package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.UserConverter;
import analu.whereio.adapters.in.web.dto.request.AtualizarNomeRequest;
import analu.whereio.adapters.in.web.dto.response.FileUploadResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioBuscaResponse;
import analu.whereio.adapters.in.web.dto.response.UsuarioPerfilResponse;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.usuario.AtualizarFotoPerfilUsecase;
import analu.whereio.application.ports.in.usuario.AtualizarNomeUsuarioUsecase;
import analu.whereio.application.ports.in.usuario.BuscarUsuariosPorPrefixoUsecase;
import analu.whereio.application.ports.in.usuario.ObterPerfilUsuarioUsecase;
import analu.whereio.application.ports.in.usuario.RemoverFotoPerfilUsecase;
import analu.whereio.application.ports.out.FileStoragePort;
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
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
class UserControllerTest {

    private static final JwtUserPrincipal PRINCIPAL = JwtUserPrincipal.testPrincipal("user-1");

    @Mock private BuscarUsuariosPorPrefixoUsecase buscarUsuariosUsecase;
    @Mock private ObterPerfilUsuarioUsecase obterPerfilUsecase;
    @Mock private AtualizarNomeUsuarioUsecase atualizarNomeUsecase;
    @Mock private AtualizarFotoPerfilUsecase atualizarFotoUsecase;
    @Mock private RemoverFotoPerfilUsecase removerFotoUsecase;
    @Mock private FileStoragePort fileStoragePort;
    @Mock private UserConverter converter;

    @InjectMocks
    private UserController userController;

    private UserAccount conta;

    @BeforeEach
    void setUp() {
        conta = new UserAccount();
        conta.setId("user-1");
        conta.setNomeUsuario("joao123");
        conta.setNome("João");
        conta.setEmail("joao@test.local");
        conta.setFotoPerfil(null);
    }

    @Nested
    @DisplayName("GET /api/usuario/buscar - buscar")
    class Buscar {

        @Test
        @DisplayName("deve retornar lista de até 10 usuários com fotoPerfilUrl preenchida")
        void deveRetornarListaComFotoPerfilUrl() {
            UserAccount comFoto = new UserAccount();
            comFoto.setId("user-2");
            comFoto.setNomeUsuario("joana");
            comFoto.setFotoPerfil("foto_key.jpg");

            UsuarioBuscaResponse resp1 = new UsuarioBuscaResponse();
            resp1.setNomeUsuario("joao123");

            UsuarioBuscaResponse resp2 = new UsuarioBuscaResponse();
            resp2.setNomeUsuario("joana");

            when(buscarUsuariosUsecase.execute("joa", "user-1")).thenReturn(List.of(conta, comFoto));
            when(converter.toBuscaResponse(conta)).thenReturn(resp1);
            when(converter.toBuscaResponse(comFoto)).thenReturn(resp2);
            when(fileStoragePort.gerarUrlAssinada("foto_key.jpg")).thenReturn("/media/foto_key.jpg");

            ResponseEntity<List<UsuarioBuscaResponse>> resposta = userController.buscar(PRINCIPAL, "joa");

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals(2, resposta.getBody().size()),
                    () -> assertNull(resposta.getBody().get(0).getFotoPerfilUrl()),
                    () -> assertEquals("/media/foto_key.jpg", resposta.getBody().get(1).getFotoPerfilUrl())
            );
        }
    }

    @Nested
    @DisplayName("GET /api/usuario/perfil - obterPerfil")
    class ObterPerfil {

        @Test
        @DisplayName("deve retornar UsuarioPerfilResponse com fotoPerfilUrl")
        void deveRetornarPerfilComFotoUrl() {
            conta.setFotoPerfil("minha_foto.jpg");
            UsuarioPerfilResponse perfilResp = new UsuarioPerfilResponse();
            perfilResp.setId("user-1");

            when(obterPerfilUsecase.execute("user-1")).thenReturn(conta);
            when(converter.toPerfilResponse(conta)).thenReturn(perfilResp);
            when(fileStoragePort.gerarUrlAssinada("minha_foto.jpg")).thenReturn("/media/minha_foto.jpg");

            ResponseEntity<UsuarioPerfilResponse> resposta = userController.obterPerfil(PRINCIPAL);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals("/media/minha_foto.jpg", resposta.getBody().getFotoPerfilUrl())
            );
        }
    }

    @Nested
    @DisplayName("PATCH /api/usuario/nome - atualizarNome")
    class AtualizarNome {

        @Test
        @DisplayName("deve atualizar nome e retornar 200 com UsuarioPerfilResponse")
        void deveAtualizarNomeERetornar200() {
            AtualizarNomeRequest req = new AtualizarNomeRequest();
            req.setNome("Novo Nome");

            UserAccount atualizado = new UserAccount();
            atualizado.setId("user-1");
            atualizado.setNome("Novo Nome");

            UsuarioPerfilResponse perfilResp = new UsuarioPerfilResponse();
            perfilResp.setNome("Novo Nome");

            when(atualizarNomeUsecase.execute("user-1", "Novo Nome")).thenReturn(atualizado);
            when(converter.toPerfilResponse(atualizado)).thenReturn(perfilResp);

            ResponseEntity<UsuarioPerfilResponse> resposta = userController.atualizarNome(PRINCIPAL, req);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals("Novo Nome", resposta.getBody().getNome())
            );
            verify(atualizarNomeUsecase).execute("user-1", "Novo Nome");
        }
    }

    @Nested
    @DisplayName("PUT /api/usuario/foto-perfil - atualizarFoto")
    class AtualizarFoto {

        @Test
        @DisplayName("deve fazer upload e retornar FileUploadResponse")
        void deveFazerUploadERetornar200() {
            MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[]{1, 2});

            when(atualizarFotoUsecase.execute(file, "user-1")).thenReturn("nova_key.jpg");
            when(fileStoragePort.gerarUrlAssinada("nova_key.jpg")).thenReturn("/media/nova_key.jpg");

            ResponseEntity<FileUploadResponse> resposta = userController.atualizarFoto(PRINCIPAL, file);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, resposta.getStatusCode()),
                    () -> assertEquals("nova_key.jpg", resposta.getBody().getFileName()),
                    () -> assertEquals("/media/nova_key.jpg", resposta.getBody().getUrlPath())
            );
        }
    }

    @Nested
    @DisplayName("DELETE /api/usuario/foto-perfil - removerFoto")
    class RemoverFoto {

        @Test
        @DisplayName("deve remover foto e retornar 204")
        void deveRemoverFotoERetornar204() {
            ResponseEntity<Void> resposta = userController.removerFoto(PRINCIPAL);

            assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
            verify(removerFotoUsecase).execute("user-1");
        }
    }
}
