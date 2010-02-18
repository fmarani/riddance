
import scala.xml._
import javax.jms.TextMessage
import scala.actors.Actor._
import scala.actors.Actor
import org.apache.log4j._

object JMSActor {
	def forwardToCore = MessageBridge.start(reactToMessages)

	def reactToMessages(JMSMessage: TextMessage) = {
        // define internal helpers for xml extraction
        def getTemplateMap(xmlseq: NodeSeq) = {
            val attrList = xmlseq map (x => Map((x\"@k").text -> (x\"@v").text))
            (attrList foldLeft Map[String,String]()) ((x:Map[String,String],y:Map[String,String]) => x++y)
        }
        
        def getTemplateBlocks(blocks: NodeSeq) = {
            def getItemsMap(items: NodeSeq) = items.toList map (item => getTemplateMap(item\"map") )
            val blockList = blocks map (block => Map((block\"@name").text -> getItemsMap(block\"item"))) 
            (blockList foldLeft Map[String,List[Map[String,String]]]()) (
                (x:Map[String,List[Map[String,String]]],y:Map[String,List[Map[String,String]]]) => x++y
            )
        }
        def getSubject(xmlbody: Elem) = (xmlbody\"template"\"subject").text
        def getTemplateText(xmlbody: Elem) = (xmlbody\"template"\"text").text
        def getTemplateHtml(xmlbody: Elem) = (xmlbody\"template"\"html").text
        def getData(xmlbody: Elem) = (xmlbody\"iterations"\"unit" map (unit => ( (unit\"@email").text, getTemplateMap(unit\"map"), getTemplateBlocks(unit\"block"))) ).toList
        // end 

        RiddanceCore.log.debug("Received JMS message: " + JMSMessage.getText)

		// message parsing
        try {
            val xmlbody = XML.loadString(JMSMessage.getText)
            val deps = new RiddanceData(
                getTemplateText(xmlbody), 
                getTemplateHtml(xmlbody), 
                getSubject(xmlbody), 
                getData(xmlbody) )
            RiddanceCore.log.debug("Injecting into core: " + deps.toString)
            RiddanceCore ! deps
        } catch {
            case exc => {
                RiddanceCore.log.debug("Error parsing XML: " + exc.toString)
                exc.printStackTrace()
                true // forcefully acknowledges badly formatted messages
            }
        }

        // discard when above threshold, then it will be picked up by another riddance process
        RiddanceCore.mailboxSize > RiddanceCore.OVERLOAD_THRESHOLD
	}
}

class RiddanceData (
    val templateText: String, 
    val templateHtml: String, 
    val subject: String,
    val data: List[ (String,Map[String,String],Map[String,List[Map[String,String]]]) ] 
) 


object RiddanceCore extends Actor {
    val OVERLOAD_THRESHOLD = 5
    lazy val log = Category.getInstance("riddance")

	def act = {
        loop {
		    react {
			    case deps: RiddanceData => {
		                    log.info("Processing request...")
                            deps.data.foreach( step => 
                                sendMail(step._1, // recipient
                                    TemplateEngine.render(deps.subject, Map(), step._2), // subject
                                    TemplateEngine.render(deps.templateText, step._3, step._2), // email text
                                    TemplateEngine.render(deps.templateHtml, step._3, step._2) // email html
                                )
                                
                            )
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
	}
	
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

