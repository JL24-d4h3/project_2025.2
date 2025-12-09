package org.project.project.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        System.out.println("❌❌❌ ===== OAUTH2 LOGIN FAILURE HANDLER EJECUTADO ===== ❌❌❌");
        System.out.println("🚨 Exception class: " + exception.getClass().getName());
        System.out.println("🚨 Exception message: " + exception.getMessage());

        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            OAuth2Error error = oauth2Exception.getError();
            System.out.println("🚨 OAuth2 Error Code: " + error.getErrorCode());
            System.out.println("🚨 OAuth2 Error Description: " + error.getDescription());
            System.out.println("🚨 OAuth2 Error URI: " + error.getUri());
        }

        System.out.println("🚨 Request URI: " + request.getRequestURI());
        System.out.println("🚨 Request Query String: " + request.getQueryString());
        System.out.println("🚨 Request Method: " + request.getMethod());

        // Log request parameters
        System.out.println("🚨 Request Parameters:");
        request.getParameterMap().forEach((key, values) -> {
            System.out.println("   " + key + " = " + String.join(", ", values));
        });

        exception.printStackTrace();

        // Redirect to signin with error parameter
        response.sendRedirect("/signin?error=oauth2_failed");
    }
}