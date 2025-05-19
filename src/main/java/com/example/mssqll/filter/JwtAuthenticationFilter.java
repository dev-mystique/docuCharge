package com.example.mssqll.filter;

import com.example.mssqll.dto.response.TokenValidationResult;
import com.example.mssqll.service.impl.JwtService;
import com.example.mssqll.service.impl.TokenBlacklistService;
import com.example.mssqll.service.impl.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            // Check if the token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Attempted to use a blacklisted token.");
                respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "You are not logged in.");
                return;
            }

            // Extract username and validate token
            String username;
            try {
                username = jwtService.extractUserName(token);
            } catch (ExpiredJwtException e) {
                log.warn("JWT token is expired: {}", e.getMessage());
                respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "JWT token is expired.");
                return;
            } catch (UnsupportedJwtException e) {
                log.warn("Unsupported JWT token: {}", e.getMessage());
                respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Unsupported JWT token.");
                return;
            } catch (MalformedJwtException e) {
                log.warn("Malformed JWT token: {}", e.getMessage());
                respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Malformed JWT token.");
                return;
            } catch (SignatureException e) {
                log.warn("Invalid JWT signature: {}", e.getMessage());
                respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Invalid JWT signature.");
                return;
            } catch (IllegalArgumentException e) {
                log.warn("JWT claims string is empty: {}", e.getMessage());
                respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "JWT claims string is empty.");
                return;
            } catch (Exception e) {
                log.error("Unexpected error during JWT parsing: {}", e.getMessage());
                respondWithError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.");
                return;
            }

            if (StringUtils.hasText(username)) {
                UserDetails userDetails = userService.userDetailsService().loadUserByUsername(username);
                TokenValidationResult validationResult = jwtService.validateToken(token, userDetails);

                if (validationResult.isValid()) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("Token validation failed: {}", validationResult.getMessage());
                    respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", validationResult.getMessage());
                    return;
                }
            } else {
                log.warn("JWT token does not contain a username.");
                respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "JWT token does not contain a username.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Sends a JSON response with error details.
     *
     * @param response The HttpServletResponse object.
     * @param status   The HTTP status code.
     * @param error    The error title.
     * @param message  The error message.
     * @throws IOException If an input or output exception occurs.
     */
    private void respondWithError(HttpServletResponse response, int status, String error, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json");

            Map<String, Object> body = new HashMap<>();
            body.put("status", status);
            body.put("error", error);
            body.put("message", message);

            objectMapper.writeValue(response.getOutputStream(), body);
        } catch (IOException e) {
            System.err.println("Failed to write error response: " + e.getMessage());
        }
    }

}