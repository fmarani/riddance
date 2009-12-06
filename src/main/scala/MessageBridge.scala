
import java.io.IOException;
import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;


class MessageBridgeListener(val callback: (TextMessage) => Boolean) extends MessageListener {
	def onMessage(message: Message) = {
        if (message.isInstanceOf[TextMessage]) {
          val textMessage = message.asInstanceOf[TextMessage];
		  if(callback(textMessage)) textMessage.acknowledge
        }
	}
}

object MessageBridge {
	def start(callback: (TextMessage) => Boolean) = {
        RiddanceCore.log.info("Starting Riddance/JMS")
		val connFactory = new ActiveMQConnectionFactory("tcp://localhost:61616")
		val conn = connFactory.createConnection
		conn.start

		val session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE)
		val inputQueue = session.createQueue("riddance-in")

		val consumer = session.createConsumer(inputQueue)

		consumer.setMessageListener(new MessageBridgeListener(callback))
	}
}


