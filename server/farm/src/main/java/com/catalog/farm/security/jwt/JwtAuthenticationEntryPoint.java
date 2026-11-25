package com.catalog.farm.security.jwt;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                authException.getMessage() != null ? authException.getMessage() : "Unauthorized"
        );
        problemDetail.setTitle("Unauthorized");
        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
