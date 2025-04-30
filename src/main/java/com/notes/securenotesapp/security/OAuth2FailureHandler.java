package com.notes.securenotesapp.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, AuthenticationException exception) throws IOException, jakarta.servlet.ServletException {
        // Check if the failure is OAuth2-specific
        if (exception instanceof OAuth2AuthenticationException) {
            // Log the OAuth2 authentication failure (you can replace this with your logging mechanism)
            System.out.println("OAuth2 Authentication failed: " + exception.getMessage());

            // Optionally, you can create a custom error response in JSON
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            String jsonResponse = "{"
                    + "\"status\": \"error\","
                    + "\"message\": \"OAuth2 Authentication failed: " + exception.getMessage() + "\""
                    + "}";

            // Write the response to the output stream
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        } else {
            // Handle non-OAuth2 related authentication failures here (if any)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
        }
    }
}
