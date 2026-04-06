package com.cinebuzz.security;

import com.cinebuzz.entity.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        final String email;
        try {
            email = jwtUtil.extractEmail(token);
        } catch (JwtException e) {
            // Expired, malformed, or bad signature — treat as anonymous so public routes still work
            log.debug("JWT not accepted: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Registration-proof JWTs are not login sessions; do not load UserDetails (user may not exist yet).
        String purpose = jwtUtil.extractPurpose(token);
        if (JwtUtil.PURPOSE_REGISTRATION_PROOF.equals(purpose)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtUtil.isTokenValid(token, userDetails)) {
                if (userDetails instanceof User u) {
                    log.debug("[jwt] Authenticated request userId={} email={} path={}", u.getId(), email,
                            request.getRequestURI());
                } else {
                    log.debug("[jwt] Authenticated request email={} path={}", email, request.getRequestURI());
                }
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}