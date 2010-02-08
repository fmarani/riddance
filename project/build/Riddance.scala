import sbt._

class RiddanceProject(info: ProjectInfo) extends DefaultProject(info)
{
	val activemq = "org.apache.activemq" % "activemq-all" % "5.2.0"
        val log4j = "log4j" % "log4j" % "1.2.14"
	val jmail = "javax.mail" % "mail" % "1.4.1"

}
