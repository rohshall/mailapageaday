package com.salquestfl.mailapageaday;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

 
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 
public class Mailer {

    public static void sendMail(String subject, String body)
        throws IOException, MessagingException {
        final Properties props = new Properties();
        //load a properties file
        props.load(new FileInputStream("mailer.properties"));

        Session session = Session.getInstance(props,
          new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(props.getProperty("user.name"), props.getProperty("user.password"));
                }
          });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(props.getProperty("user.email")));
        message.setRecipients(Message.RecipientType.TO,
            InternetAddress.parse(props.getProperty("user.email")));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);

        System.out.println("mail sent");
    }
}
