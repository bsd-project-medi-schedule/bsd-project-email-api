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

  def sendAppointmentConfirm(
    gmailSender: GmailSender,
    email: String,
    name: String,
    appointmentTime: String,
    doctorName: String,
    serviceName: String,
    officeName: String,
    confirmationToken: String
  ): IO[Unit] =
    IO.blocking {
      val confirmUrl = s"http://192.168.1.132:7000/appointment/confirm/$confirmationToken"
      val html = EmailTemplates.appointmentConfirmHtml(
        name = name,
        appointmentTime = appointmentTime,
        doctorName = doctorName,
        serviceName = serviceName,
        officeName = officeName,
        confirmUrl = confirmUrl
      )
      val messageId = gmailSender.send(email, "Confirm Your Appointment", html)
      println(s"[EmailSenders] Appointment confirmation email sent to $email, messageId: $messageId")
    }

  def sendAppointmentReminder(
    gmailSender: GmailSender,
    email: String,
    name: String,
    appointmentTime: String,
    doctorName: String,
    serviceName: String,
    officeName: String
  ): IO[Unit] =
    IO.blocking {
      val html = EmailTemplates.appointmentReminderHtml(
        name = name,
        appointmentTime = appointmentTime,
        doctorName = doctorName,
        serviceName = serviceName,
        officeName = officeName
      )
      val messageId = gmailSender.send(email, "Appointment Reminder", html)
      println(s"[EmailSenders] Appointment reminder email sent to $email, messageId: $messageId")
    }

  def sendAppointmentCancelled(
    gmailSender: GmailSender,
    email: String,
    name: String,
    appointmentTime: String,
    doctorName: String,
    serviceName: String
  ): IO[Unit] =
    IO.blocking {
      val html = EmailTemplates.appointmentCancelledHtml(
        name = name,
        appointmentTime = appointmentTime,
        doctorName = doctorName,
        serviceName = serviceName
      )
      val messageId = gmailSender.send(email, "Appointment Cancelled", html)
      println(s"[EmailSenders] Appointment cancelled email sent to $email, messageId: $messageId")
    }

  def sendDoctorWelcome(gmailSender: GmailSender, email: String, name: String): IO[Unit] =
    IO.blocking {
      val html = EmailTemplates.doctorWelcomeHtml(
        name = name,
        ctaUrl = "https://medi-schedule.com/doctor"
      )
      val messageId = gmailSender.send(email, "Welcome to MediSchedule - Doctor Portal", html)
      println(s"[EmailSenders] Doctor welcome email sent to $email, messageId: $messageId")
    }
}