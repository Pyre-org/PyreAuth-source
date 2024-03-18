package com.pyre.auth.config;

import com.pyre.auth.exception.JwtAuthenticationEntryPoint;
import com.pyre.auth.service.RedisUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtilService redisUtilService;
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {

        ProviderManager authenticationManager = (ProviderManager)authenticationConfiguration.getAuthenticationManager();

        return authenticationManager;

    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf((csrf) -> csrf.disable())
                .formLogin((formlogin) -> formlogin.disable())
                .httpBasic((httpbasic) -> httpbasic.disable())
                .exceptionHandling((except) -> except.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .headers((header) -> header.frameOptions((frame) -> frame.disable()))
                .sessionManagement((sess) -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((request) -> request.requestMatchers(
                                "/auth-service/v3/api-docs/**", "/auth-service/swagger-ui/**",
                                "/auth-service/email/send/mail",
                                "/auth-service/email/auth/check",
                                "/auth-service/user/login", "/auth-service/user/register",
                                "/auth-service/user/check/email", "/auth-service/user/check/nickname"
                                , "/auth-service/oauth/**",
                                "/auth-service/user/find/password", "/auth-service/user/reset-password/**",
                                "/auth-service/user/refresh", "/auth-service/user/logout",
                        "/auth-service/user/get/nickname/**"
                        ).permitAll()
                        .anyRequest().authenticated())

                .addFilterBefore(new JwtFilter(jwtTokenProvider, redisUtilService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
