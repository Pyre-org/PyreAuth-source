package com.pyre.auth.service;

import org.springframework.transaction.annotation.Transactional;

public interface EmailService {
    @Transactional
    String sendMail(String email);
    @Transactional
    boolean CheckAuthNum(String email, String authNum, String ip);
}
