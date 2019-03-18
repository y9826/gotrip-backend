package com.ytzl.gotrip.test.yibu;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.ytzl.gotrip.utils.common.EmptyUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

public class EmailUtil {
    // 引入javaMailSender
    private JavaMailSender mailSender;

    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    // 发送简单邮件
    public void sendSimpleMail( String to, String code) {
        if(StringUtils.isBlank(to)){
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("861024581@qq.com");
        message.setTo(to);
        message.setSubject("爱旅行");
        message.setText("用户您好，验证码：" + code);

        mailSender.send(message);
    }


    public static class Attachment {
        private String attachmentName;
        private DataSource attachmentFile;

        public Attachment(String attachmentName, DataSource attachmentFile) {
            this.attachmentName = attachmentName;
            this.attachmentFile = attachmentFile;
        }
    }
}
