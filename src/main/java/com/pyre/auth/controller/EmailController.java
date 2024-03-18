package com.pyre.auth.controller;

import com.pyre.auth.dto.request.enduser.EmailCheckDto;
import com.pyre.auth.dto.request.enduser.EmailRequestDto;
import com.pyre.auth.exception.customexception.VerifyEmailFailException;
import com.pyre.auth.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/auth-service/email")
@Tag(name="Email 인증", description = "Email 인증 API 구성")
@Validated
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/send/mail")
    @Operation(description = "이메일 인증 메일을 보냅니다.")
    public ResponseEntity<String> sendMail(@RequestBody @Valid EmailRequestDto emailRequestDto) {
        return new ResponseEntity<>(this.emailService.sendMail(emailRequestDto.email()), HttpStatus.OK);
    }

    @PostMapping("/auth/check")
    @Operation(description = "이메일의 인증을 요청합니다.")
    public ResponseEntity<?> authCheck(@RequestBody @Valid EmailCheckDto emailCheckDto, HttpServletRequest request){
        String ip = request.getRemoteAddr();
        Boolean Checked=this.emailService.CheckAuthNum(emailCheckDto.email(),emailCheckDto.authNum(), ip);
        if(Checked){
            return new ResponseEntity<>("성공적으로 이메일 인증이 되었습니다.", HttpStatus.OK);
        }
        else{
            throw new VerifyEmailFailException("인증 번호가 일치하지 않습니다. 이메일 인증은 10분 당 최대 5회까지만 가능합니다.");
        }
    }
}
