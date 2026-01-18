package config

import config.objects.*
import pureconfig.generic.semiauto.deriveConvert
import pureconfig.ConfigConvert

final case class AppConfig(
  natsConfig: NatsConfig,
  gmailConfig: GmailConfig
)

object AppConfig extends ConfigCompanionBase[AppConfig] {
  implicit override val configConvert: ConfigConvert[AppConfig] = deriveConvert[AppConfig]
}
