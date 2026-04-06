package com.cinebuzz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Builds {@link JavaMailSender} with a trimmed password so stray spaces/newlines in {@code MAIL_PASSWORD}
 * cannot cause 535 auth failures (Spring's default auto-config does not trim).
 */
@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(
            org.springframework.core.env.Environment env) {
        String host = env.getProperty("spring.mail.host", "smtp.gmail.com");
        int port = env.getProperty("spring.mail.port", Integer.class, 587);
        String username = env.getProperty("spring.mail.username", "");
        String password = env.getProperty("spring.mail.password", "");
        if (password != null) {
            password = password.trim();
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        return sender;
    }
}
