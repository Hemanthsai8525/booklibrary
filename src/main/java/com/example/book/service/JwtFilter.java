package com.example.book.service;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtFilter extends OncePerRequestFilter {

	
	 private static final String secret = "hfuybiehv7812bjhjhdfhvjkdKJHJsdfghsdfjkhfdV8785485412";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        // Public endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/user/login") || path.startsWith("/user/register")) {
            chain.doFilter(request, response);
            return;
        }

        // Get token
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            // Parse claims
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);// <<==== GET ROLE

            
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            // CREATE authentication object with ROLE
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)) // <<==== ROLE HERE
                    );

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            System.out.println("JWT Error: " + ex.getMessage());
        }

        chain.doFilter(request, response);
    }
}