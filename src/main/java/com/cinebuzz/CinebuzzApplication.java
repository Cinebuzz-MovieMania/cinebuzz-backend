package com.cinebuzz;

import com.cinebuzz.entity.User;
import com.cinebuzz.enums.Role;
import com.cinebuzz.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CinebuzzApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinebuzzApplication.class, args);
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
