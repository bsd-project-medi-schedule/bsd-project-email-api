package email

import cats.effect.IO

object EmailSenders {

  def sendWelcome(gmailSender: GmailSender, email: String, name: String): IO[Unit] =
    IO.blocking {
      val html = EmailTemplates.welcomeHtml(
        name = name,
        ctaUrl = "https://medi-schedule.com/"
      )
      val messageId = gmailSender.send(email, "Welcome to MediSchedule!", html)
      println(s"[EmailSenders] Welcome email sent to $email, messageId: $messageId")
    }

  def sendConfirmation(gmailSender: GmailSender, email: String, name: String, otp: String): IO[Unit] =
    IO.blocking {
      val confirmUrl = s"http://192.168.1.132:7000/user/confirm/$otp"
      val html = EmailTemplates.confirmAccountHtml(
        name = name,
        confirmUrl = confirmUrl
      )
      val messageId = gmailSender.send(email, "Confirm Your Email", html)
      println(s"[EmailSenders] Confirmation email sent to $email, messageId: $messageId")
    }

  def sendScheduleReminder(gmailSender: GmailSender, email: String, name: String, details: String): IO[Unit] =
    IO.blocking {
      val html = s"<p>Hi $name, this is a reminder for your schedule: $details</p>"
      val messageId = gmailSender.send(email, "Schedule Reminder", html)
      println(s"[EmailSenders] Schedule reminder sent to $email, messageId: $messageId")
    }
}