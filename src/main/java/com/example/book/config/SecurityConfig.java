package com.example.book.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.authorizeHttpRequests(auth -> auth

				// ---------- PUBLIC ----------
				.requestMatchers("/user/login", "/user/register", "/user/refresh", "/delivery/login",
						"/delivery/register", "/delivery/refresh").permitAll()
				.requestMatchers(HttpMethod.GET, "/books", "/books/**").permitAll()
				// ---------- ADMIN ----------
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/user/**","/delivery/**").hasRole("ADMIN")
				

				// ---------- AUTHENTICATED USERS ----------
				.requestMatchers("/user/me").hasAnyRole("USER", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/user/*/update-profile").hasAnyRole("USER", "ADMIN")

				// agent endpoints -> require DELIVERY_AGENT role
				.requestMatchers("/delivery/login", "/delivery/register").permitAll().requestMatchers("/delivery/**")
				.hasRole("DELIVERY_AGENT")

				// ---------- ORDERS / CART ----------
				.requestMatchers("/orders", "/orders/**").hasAnyRole("USER", "ADMIN")
				.requestMatchers("/cart", "/cart/**").hasAnyRole("USER", "ADMIN")

				// ---------- EVERYTHING ELSE ----------
				.anyRequest().authenticated());

		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
