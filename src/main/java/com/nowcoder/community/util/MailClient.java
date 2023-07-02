package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailClient.class);
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    @Value("${community.path.domain}")
    private String demain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public MailClient(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Value("${spring.mail.username}")
    private String from;

    public boolean sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException | MailSendException e) {
            LOGGER.error(String.format("发送邮件失败:%s", e.getMessage()));
            return false;
        }
        return true;

    }

    /**
     * 发送激活邮件同时检查邮箱是否存在
     */
    public boolean checkMail(User user) {
        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //http://localhost:8080/community/activation/101/code
        String url = demain +
                contextPath +
                "/activation/" +
                user.getId() +
                user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        return sendMail(user.getEmail(), "激活账号", content);
    }

    public Boolean forgetMail(String email, String code, int minutes) {
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("code", code);
        context.setVariable("minutes", minutes);
        String process = templateEngine.process("/mail/forget", context);
        return sendMail(email, "忘记密码", process);

    }

}
