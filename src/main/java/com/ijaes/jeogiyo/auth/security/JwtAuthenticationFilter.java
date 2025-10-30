package com.ijaes.jeogiyo.auth.security;

import com.ijaes.jeogiyo.auth.repository.TokenBlacklistRepository;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String bearerToken = request.getHeader("Authorization");

        try {
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);

                if (tokenBlacklistRepository.existsByToken(token)) {
                    log.warn("Token is blacklisted: {}", token);
                    request.setAttribute("exception", new CustomException(ErrorCode.BLACKLISTED_TOKEN));
                    filterChain.doFilter(request, response);
                    return;
                }

                jwtUtil.validateToken(token);

                String username = jwtUtil.extractUsername(token);

                var user = userRepository.findByUsername(username);

                if (user.isPresent()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.get(),
                                    null,
                                    user.get().getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set authentication for user: {}", username);
                } else {
                    log.warn("User not found: {}", username);
                }
            }
        } catch (CustomException ex) {
            log.warn("CustomException in JwtAuthenticationFilter: code={}, message={}", ex.getCode(), ex.getMessage());
            request.setAttribute("exception", ex);
        }

        filterChain.doFilter(request, response);
    }
}