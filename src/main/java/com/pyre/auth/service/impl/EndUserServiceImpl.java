package com.pyre.auth.service.impl;

import com.pyre.auth.clients.CommunityClient;
import com.pyre.auth.config.JwtTokenProvider;

import com.pyre.auth.dto.request.enduser.LoginDto;
import com.pyre.auth.dto.request.enduser.MyProfileEditRequest;
import com.pyre.auth.dto.request.enduser.RegisterDto;
import com.pyre.auth.dto.request.enduser.ResetPasswordRequest;
import com.pyre.auth.dto.response.enduser.*;

import com.pyre.auth.entity.EndUser;
import com.pyre.auth.enumeration.UserRoleEnum;
import com.pyre.auth.exception.customexception.*;

import com.pyre.auth.repository.EndUserRepository;
import com.pyre.auth.service.EndUserService;
import com.pyre.auth.service.RedisUtilService;
import com.pyre.auth.service.S3Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class EndUserServiceImpl implements EndUserService {
    @Value("${AWS.Domain}")
    private String AwsDomain;
    private long refreshTime = 14 * 24 * 60 * 60L;

    private final JwtTokenProvider jwtTokenProvider;
    private final EndUserRepository endUserRepository;

    private final EmailServiceImpl emailService;
    private final S3Service s3Service;
    private final JavaMailSender javaMailSender;


    private final PasswordEncoder passwordEncoder;

    private final RedisUtilService redisUtilService;
    private final CommunityClient communityClient;


    @Transactional
    @Override
    public JwtDto register(RegisterDto registerDto, String ip, HttpServletResponse response) {
        if (this.endUserRepository.existsByEmail(registerDto.email())) {
            throw new DuplicateException("다음 " + registerDto.email() + " 이메일은 이미 존재합니다.");
        }
        if (this.endUserRepository.findByNickname(registerDto.nickname()).isPresent()) {
            throw new DuplicateException("다음 " + registerDto.nickname() + " 닉네임은 이미 존재합니다.");
        }
        if (!this.emailService.CheckAuthNum(registerDto.email(), registerDto.authNum(), ip)) {
            throw new VerifyEmailFailException("이메일 인증 후 10분이 경과 되었거나 인증 번호가 일치하지 않습니다.");
        }
        EndUser savedEnduser = registerNewUser(registerDto, passwordEncoder, endUserRepository);

        String aToken = jwtTokenProvider.createToken(savedEnduser.getEmail(), savedEnduser.getRole(), savedEnduser.getId());
        String refreshToken = jwtTokenProvider.CreateRefreshToken(savedEnduser.getEmail());
//        redisTemplate.opsForValue().set(savedEnduser.getEmail(), refreshToken, refreshTime);
        this.redisUtilService.setDataExpire(savedEnduser.getEmail(), refreshToken, refreshTime);
        addTokenAndCookieToResponse(response, refreshToken, AwsDomain);
        JwtDto jwtDto = new JwtDto(aToken);

        log.info("회원가입 완료 email: {}, datetime: {}, ip address: {}", savedEnduser.getEmail(), LocalDateTime.now(), ip);
        return jwtDto;
    }

    @Transactional
    @Override
    public JwtDto login(LoginDto loginDto, HttpServletResponse response, String ip) {
        String email = loginDto.email();
        Optional<EndUser> user = this.endUserRepository.findByEmail(email);

        if (user.isPresent()) {
            EndUser siteUser = user.get();

            if (siteUser.getSocialType() == null) {
                String failed = this.redisUtilService.getData(email+"_login_failed");
                if (failed != null && Integer.parseInt(failed) > 10) {
                    throw new IncorrectPasswordException("해당 이메일의 비밀번호 인증 횟수가 10회를 초과하여 10분간 계정이 잠겼습니다.");
                }
                if (passwordEncoder.matches(loginDto.password(), siteUser.getPassword())) {

                    String aToken = jwtTokenProvider.createToken(siteUser.getEmail(), siteUser.getRole(), siteUser.getId());
                    String refreshToken = jwtTokenProvider.CreateRefreshToken(siteUser.getEmail());
//                    redisTemplate.opsForValue().set(siteUser.getEmail(), refreshToken, refreshTime);
                    this.redisUtilService.setDataExpire(siteUser.getEmail(), refreshToken, refreshTime);
                    addTokenAndCookieToResponse(response, refreshToken, AwsDomain);
                    JwtDto jwtDto = new JwtDto(aToken);

                    log.info("로그인 완료 email: {}, datetime: {}, ip address: {}", email, LocalDateTime.now(), ip);
                    log.info("{}", jwtDto);
                    siteUser.updateLastActive(LocalDateTime.now());
                    this.endUserRepository.save(siteUser);
                    return jwtDto;
                } else {
                    if (failed == null) {
                        this.redisUtilService.setDataExpire(email+"_login_failed", Integer.toString(0), 60*10L);

                    } else {
                        this.redisUtilService.setDataExpire(email+"_login_failed", Integer.toString(Integer.parseInt(failed)+1), 60*10L);
                    }
                    throw new IncorrectPasswordException("이메일과 비밀번호를 확인해주세요.");
                }
            }
            else {

                throw new IncorrectPasswordException("이메일과 비밀번호를 확인해주세요.");
            }
        } else {
            throw new IncorrectPasswordException("이메일과 비밀번호를 확인해주세요.");
        }
    }
    @Override
    public String findPassword(String email) {
        if (!this.endUserRepository.existsByEmail(email)) {
            throw new DataNotFoundException("회원가입 되지 않았습니다.");
        }
        if (this.endUserRepository.findByEmail(email).get().getSocialType() != null) {
            throw new CustomException("소셜 로그인 사용자는 비밀번호를 찾을 수 없습니다.");
        }
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[16 / 2];
        secureRandom.nextBytes(bytes);
        BigInteger bigInteger = new BigInteger(1, bytes);
        String authCode = bigInteger.toString(16);
        // Pad with zeros if the length is less than CODE_LENGTH
        while (authCode.length() < 16) {
            authCode = "0" + authCode;
        }
        String authLink = "https://pyre.live/resetPassword/"+authCode;

        String setFrom = "wodn1478@gmail.com"; // email-config에 설정한 자신의 이메일 주소를 입력
        String toMail = email;
        String title = "비밀번호 찾기 이메일 입니다."; // 이메일 제목
        String content =
                "Pyre를 방문해주셔서 감사합니다." + 	//html 형식으로 작성 !
                        "<br><br>" +
                        "비밀번호는 다음 링크에서 변경할 수 있습니다." +
                        authLink +
                        "<br>" +
                        "링크는 5분간 유효합니다. 5분이 지나면 다시 요청해주시기 바랍니다." +
                        "다른 사람에게 위 링크를 노출하지 마시기 바랍니다."; //이메일 내용 삽입
        if (this.redisUtilService.getData(toMail+"_resetCode") != null) {
            this.redisUtilService.deleteData(authCode+"_reset");
        }
        mailSend(setFrom, toMail, title, content, authCode);
        return "성공적으로 이메일 " + toMail + "에 비밀번호 초기화 링크가 전송 되었습니다. 5분 안에 변경하시기 바랍니다.";
    }
    @Transactional
    @Override
    public String resetPassword(ResetPasswordRequest resetPasswordRequest, String token) {
        String email = this.redisUtilService.getData(token+"_reset");
        if (email == null) {
            throw new PermissionFailException("비밀번호 초기화 시간이 종료 되었습니다. 다시 시도해주시기 바랍니다.");
        }
        if (resetPasswordRequest.password().equals(resetPasswordRequest.confirmPassword())) {
            EndUser endUser = this.endUserRepository.findByEmail(email).get();
            endUser.updatePassword(passwordEncoder.encode(resetPasswordRequest.password()));
            this.endUserRepository.save(endUser);
            this.redisUtilService.deleteData(token+"_reset");
            return "비밀번호가 성공적으로 변경되었습니다.";
        } else {
            throw new IncorrectPasswordException("확인 비밀번호와 비밀번호가 일치하지 않습니다.");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public CheckEmailViewDto valEmail(String email) {
        Optional<EndUser> endUser = this.endUserRepository.findByEmail(email);
        if (endUser.isPresent()) {
            CheckEmailViewDto checkEmailViewDto = new CheckEmailViewDto("이미 이 이메일이 사용되고 있습니다.", true);
            return checkEmailViewDto;
        }
        CheckEmailViewDto checkEmailViewDto = new CheckEmailViewDto("이메일이 중복되지 않습니다.", false);
        return checkEmailViewDto;
    }

    @Transactional(readOnly = true)
    @Override
    public CheckEmailViewDto valUsername(String username) {
        Optional<EndUser> endUser = this.endUserRepository.findByNickname(username);
        if (endUser.isPresent()) {
            CheckEmailViewDto checkEmailViewDto = new CheckEmailViewDto("이미 이 닉네임이 사용되고 있습니다.", true);
            return checkEmailViewDto;
        }
        CheckEmailViewDto checkEmailViewDto = new CheckEmailViewDto("닉네임이 중복되지 않습니다.", false);
        return checkEmailViewDto;
    }


    @Transactional(readOnly = true)
    @Override
    public MeDto me(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        if (!jwtTokenProvider.validateToken(accessToken, request)) {
            throw new AuthenticationFailException("토큰이 만료됨");
        }
        String email = jwtTokenProvider.getEmail(accessToken);

        if (email != null) {
            Optional<EndUser> endUser = this.endUserRepository.findByEmail(email);
            if (endUser.isPresent()) {
                EndUser siteUser = endUser.get();

                MeDto meDto = new MeDto(email, siteUser.getNickname(), siteUser.getProfilePictureUrl(), siteUser.getId(), siteUser.getRole());
                log.debug("me 완료 email: {}", email);
                return meDto;
            } else {
                throw new AuthenticationFailException("유저 정보를 찾을 수 없음. 로그인을 다시 하시기 바랍니다.");
            }


        } else {
            throw new AuthenticationFailException("유저 정보를 찾을 수 없음. 로그인을 다시 하시기 바랍니다.");
        }
    }

    @Transactional
    @Override
    public JwtDto refreshPage(String refresh_token, HttpServletResponse response, String ip) {

        if (refresh_token == null) {
            throw new AuthenticationFailException("유저 정보를 찾을 수 없음. 로그인을 다시 하시기 바랍니다.");
        }
        String email = jwtTokenProvider.getEmail(refresh_token);
//        String refresh = (String) redisTemplate.opsForValue().get(email);
        String refresh = (String) this.redisUtilService.getData(email);
        if (refresh == null || email == null) {
            throw new AuthenticationFailException("유효하지 않은 토큰");
        }
        if (!refresh.equals(refresh_token)) {
            throw new AuthenticationFailException("유효하지 않은 토큰");
        }

        EndUser endUser = this.endUserRepository.findByEmail(email).get();
        String aToken = jwtTokenProvider.createToken(email, endUser.getRole(), endUser.getId());

        endUser.updateLastActive(LocalDateTime.now());
        String refreshToken = jwtTokenProvider.CreateRefreshToken(endUser.getEmail());
        redisUtilService.setDataExpire(endUser.getEmail(), refreshToken, refreshTime);
        addTokenAndCookieToResponse(response, refreshToken, AwsDomain);
        JwtDto jwtDto = new JwtDto(aToken);
        this.endUserRepository.save(endUser);
        log.info("refreshPage 완료 email: {}, ip address: {}", email, ip);
        return jwtDto;


    }

    @Transactional
    @Override
    public void logout(String refresh_token, HttpServletResponse response) {
        String email = jwtTokenProvider.getEmail(refresh_token);
//        String refresh = (String) redisTemplate.opsForValue().get(email);
        String refresh = (String) this.redisUtilService.getData(email);
        Cookie myCookie = new Cookie("refresh_token", null);
        myCookie.setMaxAge(0);
        myCookie.setPath("/");
        myCookie.setSecure(true);  // 추후 https 구현시 true로
        myCookie.setAttribute("SameSite", "None"); // 추후 같은 사이트에서만 실행할 수 있게 변경
        myCookie.setHttpOnly(true);
        response.addCookie(myCookie);
        log.info("로그아웃 완료 email: {}", email);
        this.redisUtilService.deleteData(email);
    }
    @Transactional(readOnly = true)
    @Override
    public MyProfileResponse getMyProfile(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        if (!jwtTokenProvider.validateToken(accessToken, request)) {
            throw new AuthenticationFailException("토큰이 만료됨");
        }
        String email = jwtTokenProvider.getEmail(accessToken);
        Optional<EndUser> endUser = this.endUserRepository.findByEmail(email);
        if (!endUser.isPresent()) {
            throw new DataNotFoundException("해당 회원을 찾을 수 없습니다.");
        }
        MyProfileResponse myProfileResponse = MyProfileResponse.createDto(endUser.get());
        return myProfileResponse;
    }
    @Transactional
    @Override
    public UserProfileResponse getUserProfile(HttpServletRequest request, UUID userId) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        if (!jwtTokenProvider.validateToken(accessToken, request)) {
            throw new AuthenticationFailException("토큰이 만료됨");
        }
        Optional<EndUser> endUser = this.endUserRepository.findById(userId);
        if (!endUser.isPresent()) {
            throw new DataNotFoundException("해당 회원을 찾을 수 없습니다.");
        }
        UserProfileResponse userProfileResponse = UserProfileResponse.createDto(endUser.get());
        return userProfileResponse;
    }
    @Transactional
    @Override
    public String editMyProfile(HttpServletRequest request, MyProfileEditRequest myProfileEditRequest) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        if (!jwtTokenProvider.validateToken(accessToken, request)) {
            throw new AuthenticationFailException("토큰이 만료됨");
        }
        String email = jwtTokenProvider.getEmail(accessToken);
        Optional<EndUser> endUser = this.endUserRepository.findByEmail(email);
        if (!endUser.isPresent()) {
            throw new DataNotFoundException("해당 회원을 찾을 수 없습니다.");
        }
        EndUser gotEndUser = endUser.get();
        String spaceId = null;
        UUID channelId = null;
        if (myProfileEditRequest.selectedSpaceId() != null) {
            ResponseEntity<Boolean> canWrite =
                    communityClient.canWriteSpace(gotEndUser.getId().toString(), myProfileEditRequest.selectedSpaceId().toString());
            if (!canWrite.getBody().equals(true)) {
                throw new CustomException("해당 스페이스에 쓰기 권한이 없거나 존재하지 않는 스페이스입니다.");
            }
            ChannelInfoFromSpaceResponse channelInfoFromSpaceResponse =
                    communityClient.getChannelCaptureSpace(gotEndUser.getId().toString(), myProfileEditRequest.selectedSpaceId().toString())
                    .getBody();
            spaceId = channelInfoFromSpaceResponse.captureRoomSpaceId().toString();
            channelId = channelInfoFromSpaceResponse.channelId();
        }
        if (spaceId != null) {
            gotEndUser.updateSelectedCapture(UUID.fromString(spaceId), channelId);
        } else {
            gotEndUser.updateSelectedCapture(null, null);
        }
        gotEndUser.updateUserProfile(myProfileEditRequest);
        return "성공적으로 프로필이 수정 되었습니다.";
    }
    @Transactional(readOnly = true)
    @Override
    public FeedSpaceResponse getFeedSpace(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        if (!jwtTokenProvider.validateToken(accessToken, request)) {
            throw new AuthenticationFailException("토큰이 만료됨");
        }
        String email = jwtTokenProvider.getEmail(accessToken);
        Optional<EndUser> endUser = this.endUserRepository.findByEmail(email);
        if (!endUser.isPresent()) {
            throw new DataNotFoundException("해당 회원을 찾을 수 없습니다.");
        }
        EndUser gotEndUser = endUser.get();
        FeedSpaceResponse feedSpaceResponse = FeedSpaceResponse.makeDto(gotEndUser);
        return feedSpaceResponse;
    }

    @Transactional(readOnly = true)
    @Override
    public UserInfoFeignResponse getUserInfo(String token) {
        String jwt = null;
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            jwt = token.substring(7);
        }
        UUID id = jwtTokenProvider.getId(jwt);
        Optional<EndUser> endUser = this.endUserRepository.findById(id);
        if (!endUser.isPresent()) {
            throw new AuthenticationFailException("회원 정보를 찾을 수 없습니다.");
        }
        UserInfoFeignResponse response = UserInfoFeignResponse.createDto(endUser.get());
        return response;
    }
    @Transactional(readOnly = true)
    @Override
    public NicknameAndProfileImgResponse getNicknameAndProfileImage(String userId) {
        EndUser endUser = endUserRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new DataNotFoundException("해당 회원을 찾을 수 없습니다."));
        NicknameAndProfileImgResponse response = NicknameAndProfileImgResponse.makeDto(endUser.getNickname(), endUser.getProfilePictureUrl());
        return response;
    }

    private EndUser registerNewUser(RegisterDto registerDto, PasswordEncoder passwordEncoder, EndUserRepository endUserRepository) {
        // Create a new EndUser instance
        EndUser endUser = EndUser.createUser(
                registerDto.nickname(),
                registerDto.email(),
                passwordEncoder.encode(registerDto.password()),
                null,
                null,
                registerDto.agreement1(),
                registerDto.agreement2(),
            null,
            null,
                UserRoleEnum.ROLE_USER);
        log.info("End User Register: nickname: {}, email: {}, create_date: {}",
                registerDto.nickname(),
                registerDto.email(),
                LocalDateTime.now());
        // Save the EndUser instance to the repository
        EndUser savedEndUser = this.endUserRepository.save(endUser);

        // Return the saved EndUser instance
        return savedEndUser;
    }
    private void addTokenAndCookieToResponse(HttpServletResponse response, String refreshToken, String awsDomain) {
        // Add access token to the response header

        // Create and configure a cookie for the refresh token
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1 day
        cookie.setSecure(true);  // 추후 https 구현시 true로
        cookie.setAttribute("SameSite", "None"); // 추후 같은 사이트에서만 실행할 수 있게 변경
        cookie.setHttpOnly(true);
        cookie.setDomain(awsDomain);

        // Add the cookie to the response
        response.addCookie(cookie);
    }
    private String localDateToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        String dateString = localDateTime.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm"));

        return dateString;
    }
    @Async("threadPoolTaskExecutor")
    public void mailSend(String setFrom, String toMail, String title, String content, String authCode) {
        MimeMessage message = this.javaMailSender.createMimeMessage();//JavaMailSender 객체를 사용하여 MimeMessage 객체를 생성
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");//이메일 메시지와 관련된 설정을 수행합니다.
            // true를 전달하여 multipart 형식의 메시지를 지원하고, "utf-8"을 전달하여 문자 인코딩을 설정
            helper.setFrom(setFrom);//이메일의 발신자 주소 설정
            helper.setTo(toMail);//이메일의 수신자 주소 설정
            helper.setSubject(title);//이메일의 제목을 설정
            helper.setText(content,true);//이메일의 내용 설정 두 번째 매개 변수에 true를 설정하여 html 설정으로한다.
            this.javaMailSender.send(message);
        } catch (MessagingException e) {//이메일 서버에 연결할 수 없거나, 잘못된 이메일 주소를 사용하거나, 인증 오류가 발생하는 등 오류
            // 이러한 경우 MessagingException이 발생
            e.printStackTrace();//e.printStackTrace()는 예외를 기본 오류 스트림에 출력하는 메서드
        }
        redisUtilService.setDataExpire(toMail+"_resetCode", authCode, 60*5L);
        this.redisUtilService.setDataExpire(authCode+"_reset", toMail, 60*5L);
    }

//    @Transactional
//    public void



}
