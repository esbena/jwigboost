package dk.brics.jwig.boost.email;

import java.io.UnsupportedEncodingException;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import dk.brics.jwig.Email;
import dk.brics.xact.XML;

public class EmailHelper {

    public static Email makeEmail(String body, XML htmlBody, String subject,
            String receiverAddress) throws MessagingException,
            UnsupportedEncodingException {
        Email email = new Email();
        MimeMultipart mimeMultipart = new MimeMultipart("alternative");
    
        MimeBodyPart plain = new MimeBodyPart();
        plain.setText(body, "UTF-8");
        mimeMultipart.addBodyPart(plain);
    
        if (htmlBody != null) {
            MimeBodyPart html = new MimeBodyPart();
            XML preprendedBody = htmlBody
                    .prepend(XML
                            .parseTemplate("<style type=\"text/css\">.citation {color: gray;}</style>"));
            html.setContent(preprendedBody.close().toTemplate(),
                    "text/html; charset=\"UTF-8\"");
            mimeMultipart.addBodyPart(html);
        }
    
        String sender = "CourseAdmin";
        InternetAddress senderAddress = new InternetAddress("noreply@cs.au.dk",
                sender, "UTF-8");
        email.setSender(senderAddress);
        email.setSubject(subject, "UTF-8");
        email.setContent(mimeMultipart);
        email.setRecipient(Message.RecipientType.TO, new InternetAddress(
                receiverAddress));
    
        return email;
    }

}
