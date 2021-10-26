package me.travja.crave.emailservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@Component
public class MessageReceiver {

    private static Properties props  = new Properties();
    private static Logger     logger = LoggerFactory.getLogger(MessageReceiver.class);

    static {
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
    }

    @Value("${EMAIL}")
    private String craveEmail;
    @Value("${EMAIL_PASS}")
    private String pass;

    @RabbitListener(queues = "${RABBITMQ_QUEUE}")
    public void receiveMessage(Map<String, String> payload) {
        if (payload.get("action").equalsIgnoreCase("email")) {
            try {
                sendEmail(payload.get("email"), payload.get("subject"), payload.get("body"));
            } catch (MessagingException | IOException e) {
                System.err.println("Email send failure.");
                e.printStackTrace();
            }
        }

    }

    public void sendEmail(String email, String subject, String body) throws MessagingException, IOException {
        String from = craveEmail;
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass); //This is just an app password. lol. Will be removed
                // after this has been graded.
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from, "Crave"));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        msg.setSubject(subject);
        msg.setText(body);

        Transport.send(msg);
        logger.info("Sent email to " + email);
    }

}