package com.cinebuzz.service;

import com.cinebuzz.dto.request.CompleteRegistrationRequestDto;
import com.cinebuzz.dto.request.LoginRequestDto;
import com.cinebuzz.dto.response.AuthResponseDto;
import com.cinebuzz.entity.User;
import com.cinebuzz.enums.Role;
import com.cinebuzz.exception.AlreadyExistsException;
import com.cinebuzz.repository.UserRepository;
import com.cinebuzz.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponseDto completeRegistration(CompleteRegistrationRequestDto dto) {
        String email = jwtUtil.parseAndValidateRegistrationProofToken(dto.getRegistrationToken());
        email = RegistrationOtpService.normalizeEmail(email);

        if (userRepository.existsByEmail(email)) {
            throw new AlreadyExistsException("This email is already registered. Sign in instead.");
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        log.info("[auth] Registration completed userId={} email={}", user.getId(), user.getEmail());
        return new AuthResponseDto(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }

    public AuthResponseDto login(LoginRequestDto dto) {
        String email = RegistrationOtpService.normalizeEmail(dto.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, dto.getPassword()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generateToken(user);
        log.info("[auth] Login success userId={} email={} role={}", user.getId(), user.getEmail(), user.getRole());
        return new AuthResponseDto(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
