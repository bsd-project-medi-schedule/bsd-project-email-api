import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import config.AppConfig
import config.ConfigUtils
import config.Logging
import email.EmailSenders
import email.GmailSender

object TestEmail extends IOApp with Logging {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      cfg <- ConfigUtils.loadAndParse[AppConfig]("application.conf", "application")
      _ <- logger.info("[TestEmail] Loading Gmail configuration...")
      gmailSender = GmailSender(cfg.gmailConfig)
      _ <- logger.info("[TestEmail] Sending test welcome email...")
      _ <- EmailSenders.sendWelcome(
        gmailSender = gmailSender,
        email = "tudor.tdr.7@gmail.com",
        name = "Test User"
      )
      _ <- logger.info("[TestEmail] Test email sent successfully!")
    } yield ExitCode.Success
}