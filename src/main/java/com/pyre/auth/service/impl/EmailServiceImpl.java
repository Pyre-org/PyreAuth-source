package com.pyre.auth.service.impl;

import com.pyre.auth.exception.customexception.DuplicateException;
import com.pyre.auth.exception.customexception.VerifyEmailFailException;
import com.pyre.auth.repository.EndUserRepository;
import com.pyre.auth.service.EmailService;
import com.pyre.auth.service.RedisUtilService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final EndUserRepository endUserRepository;
    private final RedisUtilService redisUtilService;


    @Override
    @Transactional
    public String sendMail(String email) {
        if (this.endUserRepository.existsByEmail(email)) {
            throw new DuplicateException("이미 회원가입된 이메일입니다.");
        }
        String authNumber = makeRandomNumber();
        String setFrom = "wodn1478@gmail.com"; // email-config에 설정한 자신의 이메일 주소를 입력
        String toMail = email;
        String title = "회원 가입 인증 이메일 입니다."; // 이메일 제목
        String content =
                "Pyre를 방문해주셔서 감사합니다." + 	//html 형식으로 작성 !
                        "<br><br>" +
                        "인증 번호는 " + authNumber + "입니다." +
                        "<br>" +
                        "인증번호를 제대로 입력해주세요"; //이메일 내용 삽입
        mailSend(setFrom, toMail, title, content, authNumber);
        return "성공적으로 이메일 " + toMail + "에 인증 번호가 전송 되었습니다. 5분 안에 입력하시기 바랍니다.";
    }
    @Override
    @Transactional
    public boolean CheckAuthNum(String email, String authNum, String ip) {
        String failed = this.redisUtilService.getData(email+"_valid_failed");
        if (failed == null) {
            this.redisUtilService.setDataExpire(email+"_valid_failed", Integer.toString(0), 60*10L);
            failed = "0";
        } else {
            if (Integer.parseInt(failed) > 5) {
                throw new VerifyEmailFailException("해당 이메일의 인증 횟수가 초과 되었습니다. 10분 후 시도하시기 바랍니다.");
            }
        }
        if (this.endUserRepository.existsByEmail(email)) {
            throw new DuplicateException("이미 회원가입된 이메일입니다.");
        }
        if(this.redisUtilService.getData(email+"_email")==null){
            return false;
        }
        else if (this.redisUtilService.getData(email+"_email").equals(authNum)){
            return true;
        }
        else{
            this.redisUtilService.setDataExpire(email+"_valid_failed", Integer.toString(Integer.parseInt(failed)+1), 60*10L);
            return false;
        }
    }

    private String makeRandomNumber() {

        String code = UUID.randomUUID().toString().substring(0, 6); //랜덤 인증번호 uuid를 이용!
        return  code;

    }

    private void mailSend(String setFrom, String toMail, String title, String content, String authNumber) {
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
        this.redisUtilService.setDataExpire(toMail+"_email", authNumber,60*10L);
    }
}
