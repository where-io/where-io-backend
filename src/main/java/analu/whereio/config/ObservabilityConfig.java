package analu.whereio.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class ObservabilityConfig {

    @Bean
    ObservationPredicate noActuatorObservations() {
        return (name, context) -> {
            if (name.equals("http.server.requests")
                    && context instanceof ServerRequestObservationContext ctx) {
                return !ctx.getCarrier().getRequestURI().startsWith("/actuator");
            }
            return true;
        };
    }
}
