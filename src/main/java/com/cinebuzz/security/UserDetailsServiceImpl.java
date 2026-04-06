package com.cinebuzz.security;

import com.cinebuzz.repository.UserRepository;
import com.cinebuzz.service.RegistrationOtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = RegistrationOtpService.normalizeEmail(email);
        return userRepository.findByEmail(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}