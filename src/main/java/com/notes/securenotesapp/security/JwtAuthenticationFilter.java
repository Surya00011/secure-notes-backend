package com.notes.securenotesapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailService customUserDetailService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailService customUserDetailService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailService = customUserDetailService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        System.out.println("InJwtAuthenticationFilter");
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer token, continuing with filter chain.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("Extracted Token: " + token);

        try {
            String userName = jwtTokenProvider.extractUsername(token);
            System.out.println("Extracted Username: " + userName);

            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("UserName is valid. Attempting to load UserDetails.");
                UserDetails userDetails = customUserDetailService.loadUserByUsername(userName);
                System.out.println("Loaded UserDetails: " + userDetails.getUsername());

                if (jwtTokenProvider.validateToken(token, userDetails)) {
                    System.out.println("Token is valid, setting authentication.");
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    System.out.println("Invalid token.");
                }
            } else {
                System.out.println("UserName is null or authentication is already set.");
            }
        } catch (Exception e) {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("Error", "Invalid Request");
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(responseMap);
            response.getWriter().write(jsonString);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("Error occurred: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
