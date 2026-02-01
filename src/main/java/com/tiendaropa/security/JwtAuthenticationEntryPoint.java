package com.tiendaropa.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, 
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        // 1. Establecer el tipo de contenido a JSON
        response.setContentType("application/json;charset=UTF-8");
        
        // 2. Establecer el código de estado 401 (Unauthorized)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 3. Personalizar el mensaje de error para que sea claro
        String message;
        if (authException.getMessage() != null && authException.getMessage().equalsIgnoreCase("Full authentication is required to access this resource")) {
            message = "No se proporcionó un token válido para acceder a este recurso.";
        } else {
            message = authException.getMessage();
        }

        // 4. Escribir la respuesta JSON
        response.getWriter().write(String.format(
            "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\"}",
            message,
            request.getRequestURI()
        ));
    }
}