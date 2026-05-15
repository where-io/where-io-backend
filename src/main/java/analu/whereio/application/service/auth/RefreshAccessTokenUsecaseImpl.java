package analu.whereio.application.service.auth;

import analu.whereio.application.model.AuthTokens;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.auth.RefreshAccessTokenUsecase;
import analu.whereio.application.ports.out.RefreshTokenRepositoryPort;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshAccessTokenUsecaseImpl implements RefreshAccessTokenUsecase {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final UserAccountRepositoryPort userAccountRepositoryPort;
    private final AuthTokenIssuerService authTokenIssuerService;

    @Override
    public AuthTokens execute(String refreshToken) {
        MDC.put("operation", "refreshAccessToken");
        try {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new BusinessException("Refresh token obrigatório", HttpStatus.BAD_REQUEST);
            }

            String userId = refreshTokenRepositoryPort.findValidUserIdByRawToken(refreshToken)
                    .orElseThrow(() -> new BusinessException("Refresh token inválido ou expirado", HttpStatus.UNAUTHORIZED));

            refreshTokenRepositoryPort.revokeByRawToken(refreshToken);

            UserAccount user = userAccountRepositoryPort.findById(userId)
                    .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.UNAUTHORIZED));

            return authTokenIssuerService.issueForUser(user);
        } finally {
            MDC.remove("operation");
        }
    }
}
