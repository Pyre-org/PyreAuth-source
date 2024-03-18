package com.pyre.auth.config;

import com.pyre.auth.enumeration.UserRoleEnum;
import com.pyre.auth.service.RedisUtilService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static org.springframework.web.util.WebUtils.getCookie;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private JwtTokenProvider jwtTokenProvider;
    private RedisUtilService redisUtilService;
    @Value("${AWS.Domain}")
    private String awsDomain;
    private long refreshTime = 14 * 24 * 60 * 60L;

    public JwtFilter(JwtTokenProvider jwtTokenProvider, RedisUtilService redisUtilService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisUtilService = redisUtilService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null) {
            if (jwtTokenProvider.validateToken(token, request)) {
                try {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (UsernameNotFoundException ex) {
                    Cookie myCookie = new Cookie("refresh_token", null);
                    myCookie.setMaxAge(0);
                    myCookie.setPath("/");
                    myCookie.setSecure(true);  // 추후 https 구현시 true로
                    myCookie.setAttribute("SameSite", "None"); // 추후 같은 사이트에서만 실행할 수 있게 변경
                    myCookie.setHttpOnly(true);
                    response.addCookie(myCookie);
                    response.setHeader("Authorization", null);
                }
            } else {
                Cookie cookie = getCookie(request, "refresh_token");
                final String refreshToken = cookie == null ? null : cookie.getValue();
                if (refreshToken != null) {
                    if (jwtTokenProvider.validateToken(refreshToken, request)) {
                        if (redisUtilService.getData(refreshToken) == null) {
                            jwtExceptionHandler(response, "Refresh_Token Expired", HttpStatus.UNAUTHORIZED);
                            return;
                        }
                        if (!redisUtilService.getData(refreshToken).equals(refreshToken)) {
                            jwtExceptionHandler(response, "Refresh_Token is not correct", HttpStatus.UNAUTHORIZED);
                            return;
                        }
                        String refreshEmail = jwtTokenProvider.getEmail(refreshToken);
                        String accessEmail = jwtTokenProvider.getEmail(token);
                        if (!refreshEmail.equals(accessEmail)) {
                            jwtExceptionHandler(response, "Refresh_Token is not correct", HttpStatus.UNAUTHORIZED);
                            return;
                        }
                        UserRoleEnum role = jwtTokenProvider.getRole(token);
                        UUID id = jwtTokenProvider.getId(token);
                        String newAccessToken = jwtTokenProvider.createToken(refreshEmail, role, id);
                        String newRefreshToken = jwtTokenProvider.CreateRefreshToken(refreshEmail);
                        this.redisUtilService.setDataExpire(refreshEmail, newRefreshToken, refreshTime);
                        response.setHeader("Authorization", newAccessToken);
                        Cookie newCookie = new Cookie("refresh_token", newRefreshToken);
                        newCookie.setPath("/");
                        newCookie.setMaxAge(60 * 60 * 24 * 14); // 14 day
                        newCookie.setSecure(true);  // 추후 https 구현시 true로
                        newCookie.setAttribute("SameSite", "None"); // 추후 같은 사이트에서만 실행할 수 있게 변경
                        newCookie.setHttpOnly(true);
                        newCookie.setDomain(awsDomain);

                        response.addCookie(newCookie);
                        Authentication authentication = jwtTokenProvider.getAuthentication(newAccessToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    jwtExceptionHandler(response, "Refresh_Token Expired", HttpStatus.UNAUTHORIZED);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
    private void jwtExceptionHandler(HttpServletResponse response, String message, HttpStatus status) {
        response.setStatus(status.value());
        response.setContentType("application/json");
        try {

            response.sendError(status.value(), message);
        } catch (IOException e) {
            log.error("Error writing JWT exception response", e);
        }
    }

    // Consider creating a separate ErrorResponse class for standardized error responses

}