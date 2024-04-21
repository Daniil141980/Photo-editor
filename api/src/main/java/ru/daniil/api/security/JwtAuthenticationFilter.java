package ru.daniil.api.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String COOKIE_HEADER_NAME = "token";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var cookieHeader = request.getHeader(HttpHeaders.COOKIE);
        if (cookieHeader == null || !cookieHeader.startsWith(COOKIE_HEADER_NAME)) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(COOKIE_HEADER_NAME))
                .findFirst()
                .get()
                .getValue();
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (JwtException e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtService.isTokenExpired(token)) {
                var authentication = new UsernamePasswordAuthenticationToken(userDetails,
                        null, List.of(new SimpleGrantedAuthority("USER")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}