package com.example.book.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.book.service.JwtFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth

                // PUBLIC ENDPOINTS
                .requestMatchers("/user/login", "/user/register").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/books/**").permitAll()

                // ADMIN ONLY
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // USER ACCOUNT PROTECTED
                .requestMatchers(HttpMethod.GET, "/user/*").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/user/*/profile").hasAnyRole("USER", "ADMIN")

                // ORDER & CART
                .requestMatchers("/orders/**", "/cart/**").hasAnyRole("USER", "ADMIN")

                // EVERYTHING ELSE
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
