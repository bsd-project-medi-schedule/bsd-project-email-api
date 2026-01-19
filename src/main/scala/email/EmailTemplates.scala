package email

import java.nio.charset.StandardCharsets
import java.time.Year
import scala.io.Source

object EmailTemplates {

  def loadResource(path: String): String = {
    val is = Option(getClass.getClassLoader.getResourceAsStream(path))
      .getOrElse(throw new IllegalArgumentException(s"Resource not found: $path"))
    val src = Source.fromInputStream(is, StandardCharsets.UTF_8.name())
    try src.mkString
    finally {
      src.close()
      is.close()
    }
  }

  def render(template: String, values: Map[String, String]): String = {
    values.foldLeft(template) { case (acc, (k, v)) =>
      acc.replace(s"{{$k}}", v)
    }
  }

  def escapeHtml(s: String): String =
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#x27;")

  // Templates
  private val WELCOME_TEMPLATE = loadResource("email/welcome.html")
  private val CONFIRM_ACCOUNT_TEMPLATE = loadResource("email/confirm-account.html")
  private val APPOINTMENT_CONFIRM_TEMPLATE = loadResource("email/appointment-confirm.html")
  private val APPOINTMENT_REMINDER_TEMPLATE = loadResource("email/appointment-reminder.html")
  private val APPOINTMENT_CANCELLED_TEMPLATE = loadResource("email/appointment-cancelled.html")
  private val DOCTOR_WELCOME_TEMPLATE = loadResource("email/doctor-welcome.html")

  def welcomeHtml(name: String, ctaUrl: String): String = {
    render(WELCOME_TEMPLATE, Map(
      "name"   -> escapeHtml(name),
      "ctaUrl" -> ctaUrl,
      "year"   -> Year.now().toString
    ))
  }

  def confirmAccountHtml(name: String, confirmUrl: String): String = {
    render(CONFIRM_ACCOUNT_TEMPLATE, Map(
      "name"       -> escapeHtml(name),
      "confirmUrl" -> confirmUrl,
      "year"       -> Year.now().toString
    ))
  }

  def appointmentConfirmHtml(
    name: String,
    appointmentTime: String,
    doctorName: String,
    serviceName: String,
    officeName: String,
    confirmUrl: String
  ): String = {
    render(APPOINTMENT_CONFIRM_TEMPLATE, Map(
      "name"            -> escapeHtml(name),
      "appointmentTime" -> escapeHtml(appointmentTime),
      "doctorName"      -> escapeHtml(doctorName),
      "serviceName"     -> escapeHtml(serviceName),
      "officeName"      -> escapeHtml(officeName),
      "confirmUrl"      -> confirmUrl,
      "year"            -> Year.now().toString
    ))
  }

  def appointmentReminderHtml(
    name: String,
    appointmentTime: String,
    doctorName: String,
    serviceName: String,
    officeName: String
  ): String = {
    render(APPOINTMENT_REMINDER_TEMPLATE, Map(
      "name"            -> escapeHtml(name),
      "appointmentTime" -> escapeHtml(appointmentTime),
      "doctorName"      -> escapeHtml(doctorName),
      "serviceName"     -> escapeHtml(serviceName),
      "officeName"      -> escapeHtml(officeName),
      "year"            -> Year.now().toString
    ))
  }

  def appointmentCancelledHtml(
    name: String,
    appointmentTime: String,
    doctorName: String,
    serviceName: String
  ): String = {
    render(APPOINTMENT_CANCELLED_TEMPLATE, Map(
      "name"            -> escapeHtml(name),
      "appointmentTime" -> escapeHtml(appointmentTime),
      "doctorName"      -> escapeHtml(doctorName),
      "serviceName"     -> escapeHtml(serviceName),
      "year"            -> Year.now().toString
    ))
  }

  def doctorWelcomeHtml(name: String, ctaUrl: String): String = {
    render(DOCTOR_WELCOME_TEMPLATE, Map(
      "name"   -> escapeHtml(name),
      "ctaUrl" -> ctaUrl,
      "year"   -> Year.now().toString
    ))
  }
}