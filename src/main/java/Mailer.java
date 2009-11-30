
/**
compile:
javac -classpath /usr/share/java/gnumail.jar SendMail.java

run:
java -classpath /usr/share/java/gnumail.jar:/usr/share/java/gnumail-providers.jar:/usr/share/java/inetlib.jar:. SendMail localhost federico@localhost federico.marani@tangentlabs.co.uk test test
 */

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
/**
  * A simple email sender class.
  */
public class Mailer
{
  /**
    * "send" method to send the message.
    */
  public static boolean send(String smtpServer, String to, String from, String subject, String body, String html_body)
  {
    try
    {
      Properties props = System.getProperties();
      // -- Attaching to default Session, or we could start a new one --
      props.put("mail.smtp.host", smtpServer);
      Session session = Session.getDefaultInstance(props, null);
      // -- Create a new message --
      Message msg = new MimeMessage(session);
      // -- Set the FROM and TO fields --
      msg.setFrom(new InternetAddress(from));
      msg.setRecipients(Message.RecipientType.TO,
        InternetAddress.parse(to, false));
      // -- We could include CC recipients too --
      // if (cc != null)
      // msg.setRecipients(Message.RecipientType.CC
      // ,InternetAddress.parse(cc, false));
      // -- Set the subject and body text --
      msg.setSubject(subject);
      // multipart message - html and text
      Multipart mp = new MimeMultipart();

      BodyPart textPart = new MimeBodyPart();
      textPart.setText(body);   // sets type to "text/plain"

      BodyPart htmlPart = new MimeBodyPart();
      htmlPart.setContent(html_body, "text/html");

      // Collect the Parts into the MultiPart
      mp.addBodyPart(textPart);
      mp.addBodyPart(htmlPart);

      // Put the MultiPart into the Message
      msg.setContent(mp);

      // -- Set some other header information --
      msg.setHeader("X-Mailer", "Riddance/Mail");
      msg.setSentDate(new Date());
      // -- Send the message --
      Transport.send(msg);
      return true;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return false;
    }
  }
}

