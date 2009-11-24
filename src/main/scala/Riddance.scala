
import scala.util.parsing.json.JSON

object JMSActor {
	def forwardToCore = MessageBridge.start(reactToMessages)

	def reactToMessages(JMSMessage: Message) = {
		// message parsing
		val dataParsed = JSON.parseFull(JMSMessage.getText())
		dataParsed match {
			case dataParsed: Map[String, Any] => {
				val templateText: String = dataParsed.getOrElse("template-text","")
				val templateHtml: String = dataParsed.getOrElse("template-html","")
				val templateMap: Map = dataParsed.getOrElse("data", Map())
				val recipient: String = dataParsed.get("email")
				val blockMaps: Map = dataParsed.getOrElse("blkdata", Map())

				// inject dependencies through a single object
				val dependencies = new RiddanceData(recipient, templateText, templateHtml, blockMaps, templateMap)
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
    templateMap: Map[String,String]
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

