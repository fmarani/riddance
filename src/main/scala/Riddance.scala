
import scala.util.parsing.json.JSON
import javax.jms.TextMessage
import scala.actors.Actor._

object JMSActor {
	def forwardToCore = MessageBridge.start(reactToMessages)

	def reactToMessages(JMSMessage: TextMessage) = {
		// message parsing
		//val dataParsed = JSON.parseFull(JMSMessage.getText)
        val dataParsed = JSON.parseFull("{\"template-text\" : \"bar\", \"template-html\" : \"bar\", \"email\" : \"bar@bar.com\", \"blkdata\" : \"[{\"aaa\" : \"AAA\"}]\", \"data\" : \"{\"zzz\" : \"ZZZ\"}\"}")
		dataParsed match {
			case dataParsed: Map[String, Any] => {
				def e(s: String) = dataParsed get s

				val data = (e("template-text"), e("template-html"), e("email"), e("blkdata"), e("data"))

				// inject dependencies through a single pattern matched object
				val dependencies: Option[RiddanceData] = data match {
				case (Some(tt: String), Some(th: String), Some(r: String), Some(b: Map[String,List[Map[String,String]]]), Some(m: Map[String,String])) =>
					    Some(new RiddanceData(r, tt, th, b, m))
				case _ => None
				}

				RiddanceCore.inject ! dependencies
			}
		}
	}
}

class RiddanceData (
    val recipient: String, 
    val templateText: String, 
    val templateHtml: String, 
    val blockMaps: Map[String,List[Map[String,String]]], 
    val templateMap: Map[String,String]
)

object RiddanceCore {
	def inject = {
		receive {
			case deps: RiddanceData => {
		                log("Wake up on " + deps.recipient + " request")
				sendMail(deps.recipient, textRender(deps), htmlRender(deps))
			}
		        case "start" => {
                		log("Starting Riddance/Core")
			}
			case _ => {
                		log("Spurious data on JMS channels: " + _.toString)
			}
		}
	}
	
	private def textRender(deps: RiddanceData) = TemplateEngine.render(deps.templateText, deps.blockMaps, deps.templateMap)

	private def htmlRender(deps: RiddanceData) = TemplateEngine.render(deps.templateHtml, deps.blockMaps, deps.templateMap)

	private def sendMail(to: String, body: String, html: String) = {
        Mailer.send(to, "do-not-reply@tangentlabs.co.uk", body, html)
	}

    private def log(s: String) = println(s)
}

object Riddance extends Application {
    RiddanceCore.inject ! "start"
    JMSActor.forwardToCore
}

