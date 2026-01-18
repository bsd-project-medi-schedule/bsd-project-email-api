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

  // Very small "templating" helper.
  def render(template: String, values: Map[String, String]): String = {
    values.foldLeft(template) { case (acc, (k, v)) =>
      acc.replace(s"{{$k}}", v)
    }
  }

  // Optional: basic HTML escaping for user-provided fields like name.
  def escapeHtml(s: String): String =
    s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#x27;")

  private val WELCOME_TEMPLATE = loadResource("email/welcome.html")

  private val CONFIRM_ACCOUNT_TEMPLATE: String =
    loadResource("email/confirm-account.html")

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
}
