package com.notes.securenotesapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);


        response.setContentType("application/json");


        String jsonResponse = "{"
                + "\"status\": \"error\","
                + "\"message\": \"Unauthorized access: " + authException.getMessage() + "\""
                + "}";

        // Write JSON response to the output stream
        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
    }
}
