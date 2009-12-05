
import scala.util.parsing.json.JSON
import javax.jms.TextMessage
import scala.actors.Actor._
import scala.actors.Actor
import org.apache.log4j._

object JMSActor {
	def forwardToCore = MessageBridge.start(reactToMessages)

	def reactToMessages(JMSMessage: TextMessage) = {
        RiddanceCore.log.debug("Received JMS message: " + JMSMessage.getText)

		// message parsing
		val dataParsed = JSON.parseFull(JMSMessage.getText)
		dataParsed match {
			case dataParsed: Map[String, Any] => {
				def e(s: String) = dataParsed get s

				val data = (e("template-text"), e("template-html"), e("subject"), e("email"), e("blkdata"), e("data"))

				// inject dependencies through a single pattern matched object
				data match {
    				case (Some(tt: String), Some(th: String), Some(s: String), Some(r: String), Some(b: Map[String,List[Map[String,String]]]), Some(m: Map[String,String])) => {
                        val deps: RiddanceData = new RiddanceData(r, s, tt, th, b, m)
					    RiddanceCore ! deps
                    }
                    case x => {
                		RiddanceCore.log.warn("Unformatted JSON on JMS channel: " + x.toString)
        			}
				}
			}
            case x => {
                RiddanceCore.log.warn("Spurious data on JMS channel: " + x.toString)
            }
		}
        RiddanceCore.mailboxSize > RiddanceCore.OVERLOAD_THRESHOLD
	}
}

class RiddanceData (
    val recipient: String, 
    val subject: String,
    val templateText: String, 
    val templateHtml: String, 
    val blockMaps: Map[String,List[Map[String,String]]], 
    val templateMap: Map[String,String]
)

object RiddanceCore extends Actor {
    val OVERLOAD_THRESHOLD = 5
    lazy val log = Category.getInstance("riddance")

	def act = {
		receive {
			case deps: RiddanceData => {
		                log.info("Processing request...")
				        sendMail(deps.recipient, subjectRender(deps), textRender(deps), htmlRender(deps))
                        log.info("Good riddance!")
			}
	        case "start" => {
                		log.info("Riddance/Core now active")
			}
            case x => {
                		log.warn("Cannot act on received data: " + x.toString)
			}
		}
	}
	
    private def subjectRender(deps: RiddanceData) = TemplateEngine.render(deps.subject, Map(), deps.templateMap)

	private def textRender(deps: RiddanceData) = TemplateEngine.render(deps.templateText, deps.blockMaps, deps.templateMap)

	private def htmlRender(deps: RiddanceData) = TemplateEngine.render(deps.templateHtml, deps.blockMaps, deps.templateMap)

	private def sendMail(to: String, subject: String, body: String, html: String) = {
        Mailer.send(to, "do-not-reply@tangentlabs.co.uk", subject, body, html)
	}

}

object Riddance extends Application {
    RiddanceCore.log.info("Starting Riddance")
    RiddanceCore.start
    RiddanceCore ! "start"
    JMSActor.forwardToCore
}

