import akka.actor.{Actor, Props}
import akka.actor.Props.apply
import scala.concurrent.duration.DurationInt
import com.typesafe.plugin._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.GlobalSettings
import play.api.Application
import java.io.File


class EmailActor extends Actor {
  def receive = {
    case (attachment: File) ⇒ {
      //log.info("Sending mail ")
      val mail = use[MailerPlugin].email
      mail.setSubject("MailAPage email")
      mail.addRecipient("Salil Wadnerkar <rohshall@gmail.com>")
      mail.addFrom("Salil Wadnerkar <rohshall@gmail.com>")
      //sends text/text
      mail.send( "text" )
    }
  }
}

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    val emailActor = Akka.system(app).actorOf(Props[EmailActor], name = "emailActor")
    for (file <- new File("/tmp").listFiles if file.getName.endsWith(".pdf.txt")) {
      Akka.system(app).scheduler.schedule(0 seconds, 60 minutes, emailActor, file)
    }
  }
}
