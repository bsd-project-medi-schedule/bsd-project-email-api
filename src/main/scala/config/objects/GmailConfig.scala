package config.objects

import config.ConfigCompanionBase
import pureconfig.generic.semiauto.deriveConvert
import pureconfig.ConfigConvert

final case class GmailConfig(
  clientSecretPath: String
)

object GmailConfig extends ConfigCompanionBase[GmailConfig] {
  implicit override val configConvert: ConfigConvert[GmailConfig] = deriveConvert[GmailConfig]
}
