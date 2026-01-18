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

object Main extends IOApp.Simple with Logging {
  locally { val _ = EmailEvent }

  // Set thread count via system property for Cats Effect runtime
  // -Dcats.effect.workers=8 or defaults to 2x available processors
  System.setProperty(
    "cats.effect.workers",
    Math.max(8, Runtime.getRuntime.availableProcessors() + 3).toString
  )

  override def run: IO[Unit] =
    for {
      cfg <- ConfigUtils.loadAndParse[AppConfig]("application.conf", "application")
      _   <- logger.info(s"[Main] Starting email service...")
      _ <- logger.info(
        s"[Main] Connecting to NATS at ${cfg.natsConfig.natsHost}:${cfg.natsConfig.natsPort}"
      )
      gmailSender = GmailSender(cfg.gmailConfig)
      _ <- EventBus.resource(cfg.natsConfig).use { eventBus =>
        for {
          processor <- EventProcessor.create(eventBus)
          _         <- processor.register(EmailHandler(gmailSender))
          _         <- logger.info("[Main] Email handler registered, listening for events...")
          _         <- processor.run.compile.drain
        } yield ()
      }
    } yield ()
}
