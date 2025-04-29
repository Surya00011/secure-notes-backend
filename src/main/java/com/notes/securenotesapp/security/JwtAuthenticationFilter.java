package com.notes.securenotesapp.security;

import com.notes.securenotesapp.exception.AuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailService customUserDetailService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailService customUserDetailService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailService = customUserDetailService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        logger.info("In JwtAuthenticationFilter");
        logger.info("Authorization Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("No Bearer token found, continuing with filter chain.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        logger.info("Extracted Token: {}", token);

        try {
            String userName = jwtTokenProvider.extractUsername(token);
            logger.info("Extracted Username: {}", userName);

            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.info("Username is valid. Attempting to load UserDetails.");
                UserDetails userDetails = customUserDetailService.loadUserByUsername(userName);
                logger.info("Loaded UserDetails for: {}", userDetails.getUsername());

                if (jwtTokenProvider.validateToken(token, userDetails)) {
                    logger.info("Token is valid, setting authentication in context.");
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    logger.error("Invalid JWT token.");
                }
            } else {
                logger.warn("Username is null or authentication already set.");
            }
        } catch (Exception e) {
           throw new AuthenticationException("Authentication Failed"+e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
