package analu.whereio;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class WhereIoApplication {

	public static void main(String[] args) {
		Map<String, Object> defaults = new HashMap<>();
		resolveGoogleMapApiKey().ifPresent(key -> defaults.put("google.map.api.key", key));

		SpringApplication app = new SpringApplication(WhereIoApplication.class);
		if (!defaults.isEmpty()) {
			app.setDefaultProperties(defaults);
		}
		app.run(args);
	}

	private static Optional<String> resolveGoogleMapApiKey() {
		Optional<String> fromEnv =
				Optional.ofNullable(System.getenv("GOOGLE_API_KEY")).filter(s -> !s.isBlank());
		if (fromEnv.isPresent()) {
			return fromEnv;
		}
		Optional<String> fromDotEnv =
				Optional.ofNullable(
								Dotenv.configure().ignoreIfMissing().load().get("GOOGLE_API_KEY"))
						.filter(s -> !s.isBlank());
		return fromDotEnv;
	}

}
