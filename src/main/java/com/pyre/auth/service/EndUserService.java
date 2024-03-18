package com.pyre.auth.service;


import com.pyre.auth.dto.request.enduser.LoginDto;
import com.pyre.auth.dto.request.enduser.MyProfileEditRequest;
import com.pyre.auth.dto.request.enduser.RegisterDto;
import com.pyre.auth.dto.request.enduser.ResetPasswordRequest;
import com.pyre.auth.dto.response.enduser.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


public interface EndUserService {

    @Transactional
    JwtDto register(RegisterDto registerDto, String ip, HttpServletResponse response);
    @Transactional
    JwtDto login(LoginDto loginDto, HttpServletResponse response, String ip);
    @Transactional
    String findPassword(String email);
    @Transactional
    String resetPassword(ResetPasswordRequest resetPasswordRequest, String token);
    @Transactional(readOnly = true)
    CheckEmailViewDto valEmail(String email);
    @Transactional(readOnly = true)
    CheckEmailViewDto valUsername(String username);
    @Transactional(readOnly = true)
    MeDto me(HttpServletRequest request);
    @Transactional
    JwtDto refreshPage(String refresh_token, HttpServletResponse response, String ip);
    @Transactional
    void logout(String refresh_token, HttpServletResponse response);
    @Transactional(readOnly = true)
    MyProfileResponse getMyProfile(HttpServletRequest request);
    @Transactional(readOnly = true)
    UserProfileResponse getUserProfile(HttpServletRequest request, UUID userId);
    @Transactional
    String editMyProfile(HttpServletRequest request, MyProfileEditRequest myProfileEditRequest);
    @Transactional(readOnly = true)
    FeedSpaceResponse getFeedSpace(HttpServletRequest request);
    @Transactional(readOnly = true)
    UserInfoFeignResponse getUserInfo(String token);
    @Transactional(readOnly = true)
    NicknameAndProfileImgResponse getNicknameAndProfileImage(String userId);
}
