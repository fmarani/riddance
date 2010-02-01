
/**
compile:
-classpath /usr/share/java/gnumail.jar

run:
-classpath /usr/share/java/gnumail.jar:/usr/share/java/gnumail-providers.jar:/usr/share/java/inetlib.jar:. 
 */

import javax.mail._
import javax.mail.internet._
import java.util._

object Mailer {
    def send(to: String, from: String, subject: String, body: String, htmlBody: String) = {
        try {
            val props = System.getProperties()
            props.put("mail.smtp.host", "localhost")

            // -- Attaching to default Session, or we could start a new one --
            val session = Session.getDefaultInstance(props, null)

            val msg = new MimeMessage(session)
            msg.setFrom(new InternetAddress(from))
            //msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false))
            msg.setRecipients(Message.RecipientType.TO, to)
            msg.setSubject(subject)

            // multipart message - html and text
            val mp = new MimeMultipart()

            val textPart = new MimeBodyPart()
            textPart.setText(body)
            val htmlPart = new MimeBodyPart()
            htmlPart.setContent(htmlBody, "text/html")

            mp.addBodyPart(textPart)
            mp.addBodyPart(htmlPart)
            msg.setContent(mp)

            // -- Set some other header information --
            msg.setHeader("X-Mailer", "Riddance/Mail")
            msg.setSentDate(new Date())

            Transport.send(msg)

            true
        } catch {
            case exc => {
                RiddanceCore.log.debug("Error sending email: " + exc.toString)
                exc.printStackTrace()
                false
            }
        }
    }
}

