package com.pyre.auth.controller;


import com.pyre.auth.dto.request.oauth.OauthDto;
import com.pyre.auth.dto.response.enduser.JwtDto;
import com.pyre.auth.enumeration.OAuthServerType;
import com.pyre.auth.service.Oauth2Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/auth-service/oauth")
@Tag(name="OAuth2", description = "OAUTH2 API 구성")
@Validated
public class Oauth2Controller {
    private final Oauth2Service oauth2Service;

    @SneakyThrows
    @GetMapping("/{oauthServerType}")
    public ResponseEntity<Void> redirectAuthCodeRequestUrl(
            @PathVariable OAuthServerType oauthServerType,
            HttpServletResponse response
    ) {
        String redirectUrl = this.oauth2Service.getAuthCodeRequestUrl(oauthServerType);
        response.sendRedirect(redirectUrl);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/login/{oauthServerType}")
    public ResponseEntity<JwtDto> login(
            @PathVariable OAuthServerType oauthServerType,
            @RequestBody @Valid OauthDto code,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        JwtDto login = this.oauth2Service.login(oauthServerType, code.code(), response, request.getRemoteAddr());
        return ResponseEntity.ok(login);
    }

}
