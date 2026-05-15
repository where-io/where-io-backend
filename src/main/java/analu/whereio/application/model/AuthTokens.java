package analu.whereio.application.model;

public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds) {
}
