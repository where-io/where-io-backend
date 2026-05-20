package analu.whereio.application.ports.in.auth;

import analu.whereio.application.model.AuthTokens;

public interface RefreshAccessTokenUsecase {

    AuthTokens execute(String refreshToken);
}
