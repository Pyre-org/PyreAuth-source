package com.pyre.auth.controller;

import com.pyre.auth.dto.request.enduser.*;
import com.pyre.auth.dto.response.enduser.*;
import com.pyre.auth.exception.customexception.VerifyEmailFailException;
import com.pyre.auth.service.EndUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/auth-service/user")
@Tag(name="EndUser", description = "EndUser API 구성")
@Validated
public class EndUserController {
    private final EndUserService endUserService;

    @Operation(description = "회원가입")
    @PostMapping("/register")
    @Parameters({
            @Parameter(name = "registerDto", description = "회원가입 바디", required = true)
    })
    public ResponseEntity<JwtDto> register(@RequestBody @Valid RegisterDto registerDto, HttpServletRequest request, HttpServletResponse response) {

        return new ResponseEntity<>(this.endUserService.register(registerDto, request.getRemoteAddr(), response), HttpStatus.OK);

    }

    @Operation(description = "로그인")
    @PostMapping("/login")
    @Parameters({
            @Parameter(name = "loginDto", description = "로그인 바디", required = true)
    })
    public ResponseEntity<?> login(@RequestBody @Valid LoginDto loginDto, HttpServletResponse httpServletResponse, HttpServletRequest request) {
        try {
            return new ResponseEntity<>( this.endUserService.login(loginDto, httpServletResponse, request.getRemoteAddr()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("이메일과 비밀번호를 확인바랍니다.", HttpStatus.BAD_REQUEST);
        }
    }
    @Operation(description = "비밀번호 찾기")
    @PostMapping("/find/password")
    public ResponseEntity<?> findPassword(@RequestBody @Valid FindPasswordRequest findPasswordRequest) {
        return new ResponseEntity<>(this.endUserService.findPassword(findPasswordRequest.email()), HttpStatus.OK);
    }
    @Operation(description = "비밀번호 초기화 링크")
    @PostMapping("/resetPassword/{token}")
    public ResponseEntity<?> resetPassword(
            @RequestBody @Valid ResetPasswordRequest resetPasswordRequest,
            @PathVariable("token") String token
    ) {
        return new ResponseEntity<>(this.endUserService.resetPassword(resetPasswordRequest, token), HttpStatus.OK);
    }
    @Operation(description = "이메일 중복 확인")
    @PostMapping("/check/email")
    @Parameters({
            @Parameter(name = "email", description = "중복 확인 이메일 바디", required = true),
    })
    public ResponseEntity<CheckEmailViewDto> valEmail(@RequestBody @Valid CheckEmailDto email) {
        CheckEmailViewDto checkEmailViewDto = this.endUserService.valEmail(email.email());
        return new ResponseEntity<>(checkEmailViewDto, HttpStatus.OK);
    }

    @Operation(description = "닉네임 중복 확인")
    @PostMapping("/check/nickname")
    @Parameters({
            @Parameter(name = "nickname", description = "중복 확인 닉네임 바디", required = true),
    })
    public ResponseEntity<CheckEmailViewDto> valUsername(@RequestBody @Valid CheckNicknameDto nickname) {
        CheckEmailViewDto checkEmailViewDto = this.endUserService.valUsername(nickname.nickname());
        return new ResponseEntity<>(checkEmailViewDto, HttpStatus.OK);
    }

    @Operation(description = "유저 확인")
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        try {
            return new ResponseEntity<>(this.endUserService.me(request), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

    }
    @Parameters({
            @Parameter(name = "cookie", description = "리프레시 토큰 쿠키", required = true, example = "eyASdfqr.fasd", in = ParameterIn.COOKIE),
    })
    @Operation(description = "리프레시")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshPage(@CookieValue(value = "refresh_token", required = false) Cookie cookie, HttpServletResponse response, HttpServletRequest request) {
        if (cookie == null) {
            throw new VerifyEmailFailException("리프레시 토큰이 없습니다.");
        }
        String refreshToken = cookie.getValue();
        if (refreshToken == null) {
            throw new VerifyEmailFailException("리프레시 토큰이 없습니다.");
        }

        return new ResponseEntity<>(this.endUserService.refreshPage(refreshToken, response, request.getRemoteAddr()), HttpStatus.OK);
    }

    @Operation(description = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.error(cookie.getValue());
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        this.endUserService.logout(refreshToken, response);
        return new ResponseEntity<>("로그아웃 되었습니다.", HttpStatus.OK);
    }
    @Operation(description = "유저 마이 프로필 불러오기")
    @GetMapping("/profile/my")
    public ResponseEntity<MyProfileResponse> getMyProfile(
            HttpServletRequest request
    ) {
        return new ResponseEntity<>(this.endUserService.getMyProfile(request), HttpStatus.OK);
    }
    @Operation(description = "유저 마이 프로필 수정하기")
    @PutMapping("/profile/my/edit")
    public ResponseEntity<String> editMyProfile(
            HttpServletRequest request,
            @RequestBody @Valid MyProfileEditRequest myProfileEditRequest
            ) {
        return new ResponseEntity<>(this.endUserService.editMyProfile(request, myProfileEditRequest), HttpStatus.OK);
    }
    @Operation(description = "유저 프로필 불러오기")
    @GetMapping("/profile/{userId}")
    @Parameters({
            @Parameter(name = "userId", description = "유저 UUID", required = true, in = ParameterIn.PATH),
    })
    public ResponseEntity<UserProfileResponse> getUserProfile(
            HttpServletRequest request,
            @PathVariable UUID userId
    ) {
        return new ResponseEntity<>(this.endUserService.getUserProfile(request, userId), HttpStatus.OK);
    }
    @Operation(description = "유저 정보 가져오기 Feign 클라이언트 용")
    @GetMapping("/info")
    @Parameters({
            @Parameter(name = "token", description = "액세스 토큰", required = true, in = ParameterIn.HEADER),
    })
    public UserInfoFeignResponse getUserInfo(@RequestHeader("Authorization") String token) {
        return this.endUserService.getUserInfo(token);
    }
    @GetMapping("/feedSpace")
    @Operation(description = "피드 설정 정보를 가져올 수 있음")
    public ResponseEntity<FeedSpaceResponse> getFeedSpace(
            HttpServletRequest request
    ) {
        return new ResponseEntity<>(this.endUserService.getFeedSpace(request), HttpStatus.OK);
    }
    @GetMapping("/get/nickname/{userId}")
    @Operation(description = "유저 닉네임 및 프로필 사진 가져오기 Feign 용")
    public ResponseEntity<NicknameAndProfileImgResponse> getNickname(
            @PathVariable String userId
    ) {
        return new ResponseEntity<>(this.endUserService.getNicknameAndProfileImage(userId), HttpStatus.OK);
    }

}
