package analu.whereio.adapters.in.web;

import analu.whereio.adapters.in.web.dto.request.LoginRequest;
import analu.whereio.adapters.in.web.dto.request.RefreshTokenRequest;
import analu.whereio.adapters.in.web.dto.request.RegisterRequest;
import analu.whereio.adapters.in.web.dto.response.AuthTokensResponse;
import analu.whereio.application.model.AuthTokens;
import analu.whereio.application.ports.in.auth.LoginUserUsecase;
import analu.whereio.application.ports.in.auth.RefreshAccessTokenUsecase;
import analu.whereio.application.ports.in.auth.RegisterUserUsecase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUsecase registerUserUsecase;
    private final LoginUserUsecase loginUserUsecase;
    private final RefreshAccessTokenUsecase refreshAccessTokenUsecase;

    @PostMapping("/register")
    public ResponseEntity<AuthTokensResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthTokens tokens = registerUserUsecase.execute(
                request.getEmail(), request.getPassword(), request.getNome(), request.getNomeUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(tokens));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokens tokens = loginUserUsecase.execute(request.getIdentifier(), request.getPassword());
        return ResponseEntity.ok(toResponse(tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokens tokens = refreshAccessTokenUsecase.execute(request.getRefreshToken());
        return ResponseEntity.ok(toResponse(tokens));
    }

    private static AuthTokensResponse toResponse(AuthTokens tokens) {
        return new AuthTokensResponse(tokens.accessToken(), tokens.refreshToken(), "Bearer", tokens.expiresInSeconds());
    }
}
