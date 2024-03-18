package com.pyre.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
//    private final ObjectMapper objectMapper;
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.debug("commence 진입 {}", response);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        log.error("commence 진입 {}", response);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("로그인 정보를 확인할 수 없습니다. 다시 로그인 해주세요.");
        response.getWriter().flush();
        log.debug("commence 진입 {}", response);
    }
}
