package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.converter.AmigosConverter;
import analu.whereio.adapters.in.web.dto.response.AmigoPerfilResponse;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.amigos.ListarAmigosUsecase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalAmigosController")
class InternalAmigosControllerTest {

    @Mock private ListarAmigosUsecase listarAmigosUsecase;
    @Mock private AmigosConverter converter;
    @InjectMocks private InternalAmigosController controller;

    private UserAccount userAccount;
    private AmigoPerfilResponse amigoResponse;

    @BeforeEach
    void setUp() {
        userAccount = new UserAccount();
        userAccount.setId("friend-1");
        userAccount.setNome("João");
        userAccount.setNomeUsuario("joao123");
        userAccount.setEmail("joao@email.com");

        amigoResponse = new AmigoPerfilResponse();
        amigoResponse.setId("friend-1");
        amigoResponse.setNome("João");
        amigoResponse.setNomeUsuario("joao123");
        amigoResponse.setEmail("joao@email.com");
    }

    @Nested
    @DisplayName("GET /internal/amigos/{userId}")
    class ListarAmigos {

        @Test
        @DisplayName("returns 200 with friend list for given userId")
        void returnsFriendList() {
            when(listarAmigosUsecase.execute("user-abc")).thenReturn(List.of(userAccount));
            when(converter.toAmigoResponse(userAccount)).thenReturn(amigoResponse);

            ResponseEntity<List<AmigoPerfilResponse>> response = controller.listarAmigos("user-abc");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getId()).isEqualTo("friend-1");
        }

        @Test
        @DisplayName("returns 200 with empty list when user has no friends")
        void returnsEmptyList() {
            when(listarAmigosUsecase.execute("user-abc")).thenReturn(List.of());

            ResponseEntity<List<AmigoPerfilResponse>> response = controller.listarAmigos("user-abc");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }
}
