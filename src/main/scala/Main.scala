import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import config.AppConfig
import config.ConfigUtils
import config.Logging
import email.GmailSender
import nats.EventBus
import nats.EventProcessor
import natstools.events.EmailEvent
import natstools.handlers.EmailHandler

object Main extends IOApp with Logging {
  locally { val _ = EmailEvent }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      cfg <- ConfigUtils.loadAndParse[AppConfig]("application.conf", "application")
      _   <- logger.info(s"[Main] Starting email service...")
      _ <- logger.info(
        s"[Main] Connecting to NATS at ${cfg.natsConfig.natsHost}:${cfg.natsConfig.natsPort}"
      )
      gmailSender = GmailSender(cfg.gmailConfig)
      exitCode <- EventBus.resource(cfg.natsConfig).use { eventBus =>
        for {
          processor <- EventProcessor.create(eventBus)
          _         <- processor.register(EmailHandler(gmailSender))
          _         <- logger.info("[Main] Email handler registered, listening for events...")
          _         <- processor.run.compile.drain
        } yield ExitCode.Success
      }
    } yield exitCode
}
