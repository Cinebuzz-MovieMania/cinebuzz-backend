package com.cinebuzz;

import com.cinebuzz.entity.User;
import com.cinebuzz.enums.Role;
import com.cinebuzz.repository.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@EnableAsync
public class CinebuzzApplication {

	public static void main(String[] args) {
		loadDotenv();
		SpringApplication.run(CinebuzzApplication.class, args);
	}

	/**
	 * Loads {@code .env} from the process working directory (run Maven/IDE from {@code backend/Cinebuzz}),
	 * or from {@code backend/Cinebuzz/.env} when the working directory is the repo root.
	 * Does not override keys already set in the OS environment.
	 */
	private static void loadDotenv() {
		Path[] candidates = new Path[] {
				Path.of(".env"),
				Path.of("backend", "Cinebuzz", ".env"),
		};
		Path envFile = null;
		for (Path p : candidates) {
			if (Files.isRegularFile(p)) {
				envFile = p.toAbsolutePath();
				break;
			}
		}
		if (envFile == null) {
			return;
		}
		String dir = envFile.getParent().toString();
		Dotenv dotenv = Dotenv.configure()
				.directory(dir)
				.ignoreIfMissing()
				.load();
		dotenv.entries().forEach(entry -> {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null || System.getenv(key) != null) {
				return;
			}
			System.setProperty(key, value);
		});
	}

	@Bean
	CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByEmail("admin@cinebuzz.com")) {
				User admin = new User();
				admin.setName("Super Admin");
				admin.setEmail("admin@cinebuzz.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(Role.ROLE_ADMIN);
				userRepository.save(admin);
				System.out.println("Default admin seeded: admin@cinebuzz.com / admin123");
			}
		};
	}
}
