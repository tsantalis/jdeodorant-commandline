package ca.concordia.jdeodorant.eclipse.commandline.utility;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailer {
	
	private final Authenticator mailAuthenticator;
	
	public enum SecurityType {
		NONE,
		SSL,
		STARTLS;
	}

	public Mailer(String host, int port, boolean authenticated, SecurityType securityType, String userName, String password) {
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);
		if (securityType == SecurityType.SSL) {
			props.put("mail.smtp.socketFactory.port", port);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.imap.ssl.enable", "true");
		}
		if (securityType == SecurityType.STARTLS) {
			props.put("mail.smtp.starttls.enable", "true");
		}
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.ssl.trust", host);
		if (authenticated) {
			props.put("mail.smtp.auth", "true");
		}
		
		mailAuthenticator = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		};
		
	}

	public boolean sendMail(String subject, String body, String from, String... to) {
		Session session = Session.getInstance(System.getProperties(), mailAuthenticator);
		try {
			for (String address : to) {
				if (!"".equals(address.trim())) {
					MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(from));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
					message.setSubject(subject);
					message.setText(body);
					Transport.send(message);
				}
			}
			return true;
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
		return false;
	}
	
}
