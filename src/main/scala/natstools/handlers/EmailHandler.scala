package natstools.handlers

import cats.effect.IO
import config.Logging
import email.{EmailSenders, GmailSender}
import nats.{EventHandler, NatsEvent}
import natstools.events.EmailEvent

final case class EmailHandler(gmailSender: GmailSender) extends EventHandler with Logging {
  override val handles: String = "message.email"

  override def handle(event: NatsEvent): IO[List[NatsEvent]] = event match {
    case e: EmailEvent =>
      println(s"received $e")
      logger.info(s"receved this: $e")
      val sendAction: IO[Unit] = parsePurpose(e.purpose) match {
        case Some(("user", "welcome")) =>
          logger.info("Received welcome")
          val name = e.metadata.getOrElse("name", "User")
          EmailSenders.sendWelcome(gmailSender, e.email, name)

        case Some(("user", "confirm")) =>
          val otp = e.metadata.getOrElse("otp", "")
          val name = e.metadata.getOrElse("name", "User")
          EmailSenders.sendConfirmation(gmailSender, e.email, name, otp)

        case Some(("schedule", "reminder")) =>
          val details = e.metadata.getOrElse("details", "")
          val name = e.metadata.getOrElse("name", "User")
          EmailSenders.sendScheduleReminder(gmailSender, e.email, name, details)

        case Some((kind, operator)) =>
          logger.info(s"[EmailHandler] Unhandled email type: kind=$kind, operator=$operator")

        case None =>
          logger.info(s"[EmailHandler] Invalid purpose format: ${e.purpose}")
      }
      sendAction.start.void.as(List.empty[NatsEvent])

    case _ => IO.pure(List.empty)
  }

  private def parsePurpose(purpose: String): Option[(String, String)] = {
    // Expected format: email.{kind}.{operator}
    val parts = purpose.split('.')
    if (parts.length == 3 && parts(0) == "email") {
      Some((parts(1), parts(2)))
    } else {
      None
    }
  }
}