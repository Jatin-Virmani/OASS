package com.oass.service;

import com.oass.util.Config;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles sending emails via standard Jakarta Mail (SMTP).
 * If SMTP configuration is missing, it logs the email to console/logs
 * so testing works smoothly without needing an actual SMTP server.
 */
public final class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());

    private EmailService() {}

    /**
     * Sends an email notification.
     * @param to recipient email address
     * @param subject email subject line
     * @param body email body text
     */
    public static boolean sendEmail(String to, String subject, String body) {
        String host = Config.get("smtp.host", "");

        // If SMTP host is not configured, simulate by logging
        if (host.isBlank()) {
            LOG.info("[SIMULATED EMAIL NOTIFICATION]\nTo: " + to + "\nSubject: " + subject + "\nBody:\n" + body + "\n------------------------------------");
            return true;
        }

        try {
            String port = Config.get("smtp.port", "587");
            String user = Config.get("smtp.user", "");
            String pass = Config.get("smtp.password", "");
            String from = Config.get("smtp.from", "noreply@mru.edu");

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "MRU Academic Portal"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            LOG.info("Email successfully sent to: " + to);
            return true;

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send email to " + to + ": " + e.getMessage(), e);
            return false;
        }
    }
}