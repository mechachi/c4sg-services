package org.c4sg.service.impl;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.c4sg.service.AsyncEmailService;
import org.c4sg.service.EmailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Service
public class AsyncEmailServiceImpl implements AsyncEmailService {
	
	@Autowired
	private EmailTemplateService service;
	
	@Autowired
	private JavaMailSender mailer;
	
    /**
     * Sends an email message asynchronously.
     *
     * @param from       email address from which the message will be sent.
     * @param recipient  array of strings containing the recipients of the message.
     * @param subject    subject header field.
     * @param text       content of the message.
     */
    @Async
    public void send(String from, String recipient, String subject, String text) {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream("/mail.properties"));
            String host = properties.getProperty("mail.smtp.host");
            String port = properties.getProperty("mail.smtp.port");
            String ssl = properties.getProperty("mail.smtp.ssl.enable");
            String username = properties.getProperty("mail.smtp.username");
            String password = properties.getProperty("mail.smtp.password");
            HtmlEmail email = new HtmlEmail();
            email.setHostName(host);
            email.setSmtpPort(Integer.parseInt(port));
            email.setAuthentication(username, password);
            email.setSSLOnConnect(Boolean.parseBoolean(ssl));
            Objects.requireNonNull(from, "Sender's email is incorrect");
            Objects.requireNonNull(recipient, "Recipient's email is incorrect");
            email.setFrom(from);
            email.addTo(recipient);
            email.setSubject(subject);
            email.setHtmlMsg(text);
            email.send();
            System.out.println("Email sent to " + recipient + " successfully");
        } catch (EmailException e) {
            e.printStackTrace();
            System.out.println("Fail: Could not send email to: " + recipient + " !!! ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void sendWithContext(String from, String recipient, String subject, String template, Map<String, Object> mailContext) {
		String text = service.generateFromContext(mailContext, template);
		
		MimeMessagePreparator msgPrep = m -> {
			MimeMessageHelper helper = new MimeMessageHelper(m);
			helper.setFrom(from);
			helper.setTo(recipient);
			helper.setSubject(subject);
			helper.setText(text, true);
		};
		
		mailer.send(msgPrep);
//		System.out.println(text);
	}
}

