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
      logger.info(s"[EmailHandler] Received: $e")
      val sendAction: IO[Unit] = parsePurpose(e.purpose) match {
        case Some(("user", "welcome")) =>
          val name = e.metadata.getOrElse("name", "User")
          EmailSenders.sendWelcome(gmailSender, e.email, name)

        case Some(("user", "confirm")) =>
          val otp = e.metadata.getOrElse("otp", "")
          val name = e.metadata.getOrElse("name", "User")
          EmailSenders.sendConfirmation(gmailSender, e.email, name, otp)

        case Some(("doctor", "welcome")) =>
          val name = e.metadata.getOrElse("name", "Doctor")
          EmailSenders.sendDoctorWelcome(gmailSender, e.email, name)

        case Some(("appointment", "confirm")) =>
          val name = e.metadata.getOrElse("name", "Patient")
          val appointmentTime = e.metadata.getOrElse("appointmentTime", "")
          val doctorName = e.metadata.getOrElse("doctorName", "")
          val serviceName = e.metadata.getOrElse("serviceName", "")
          val officeName = e.metadata.getOrElse("officeName", "")
          val confirmationToken = e.metadata.getOrElse("confirmationToken", "")
          EmailSenders.sendAppointmentConfirm(
            gmailSender, e.email, name, appointmentTime,
            doctorName, serviceName, officeName, confirmationToken
          )

        case Some(("appointment", "reminder")) =>
          val name = e.metadata.getOrElse("name", "Patient")
          val appointmentTime = e.metadata.getOrElse("appointmentTime", "")
          val doctorName = e.metadata.getOrElse("doctorName", "")
          val serviceName = e.metadata.getOrElse("serviceName", "")
          val officeName = e.metadata.getOrElse("officeName", "")
          EmailSenders.sendAppointmentReminder(
            gmailSender, e.email, name, appointmentTime,
            doctorName, serviceName, officeName
          )

        case Some(("appointment", "cancelled")) =>
          val name = e.metadata.getOrElse("name", "Patient")
          val appointmentTime = e.metadata.getOrElse("appointmentTime", "")
          val doctorName = e.metadata.getOrElse("doctorName", "")
          val serviceName = e.metadata.getOrElse("serviceName", "")
          EmailSenders.sendAppointmentCancelled(
            gmailSender, e.email, name, appointmentTime,
            doctorName, serviceName
          )

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