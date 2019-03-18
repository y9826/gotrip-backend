package com.ytzl.gotrip.test.yibu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@EnableAspectJAutoProxy(exposeProxy = true)
public class SendService {
    @Autowired
    private JavaMailSender mailSender;

    @Async//注意此注解,有坑
    public void asyncSendEmail( String to,String code) throws Exception {
        EmailUtil emailUtils = new EmailUtil(mailSender);
        emailUtils.sendSimpleMail(to,code);
    }
}
