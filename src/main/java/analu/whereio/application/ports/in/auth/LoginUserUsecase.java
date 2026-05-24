package analu.whereio.application.ports.in.auth;

import analu.whereio.application.model.AuthTokens;

public interface LoginUserUsecase {

    AuthTokens execute(String identifier, String rawPassword);
}
