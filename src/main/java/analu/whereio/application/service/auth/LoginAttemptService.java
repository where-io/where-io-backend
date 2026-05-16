package analu.whereio.application.service.auth;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    static final int MAX_ATTEMPTS = 3;
    static final Duration LOCKOUT_DURATION = Duration.ofMinutes(10);

    private final Clock clock;
    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void recordFailure(String email) {
        attempts.compute(email, (key, record) -> {
            int newCount = (record == null ? 0 : record.count) + 1;
            Instant lockedUntil = newCount >= MAX_ATTEMPTS
                    ? Instant.now(clock).plus(LOCKOUT_DURATION)
                    : (record != null ? record.lockedUntil : null);
            return new AttemptRecord(newCount, lockedUntil);
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(email);
    }

    public Optional<Duration> getLockoutRemaining(String email) {
        AttemptRecord record = attempts.get(email);
        if (record == null || record.lockedUntil == null) {
            return Optional.empty();
        }
        Duration remaining = Duration.between(Instant.now(clock), record.lockedUntil);
        if (remaining.isNegative() || remaining.isZero()) {
            attempts.remove(email);
            return Optional.empty();
        }
        return Optional.of(remaining);
    }

    record AttemptRecord(int count, Instant lockedUntil) {}
}
