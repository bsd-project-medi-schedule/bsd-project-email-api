package email

import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import config.objects.GmailConfig
import jakarta.mail.Session
import jakarta.mail.internet.{InternetAddress, MimeMessage}

import java.io.{File, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Base64
import java.util.Properties

final case class GmailSender(gmailConfig: GmailConfig) {
  private val JSON_FACTORY = GsonFactory.getDefaultInstance
  private val TOKENS_DIR = new File("tokens") // do not commit this folder

  private val SERVICE = gmailService()

  private def gmailService(): Gmail = {
    val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()

    if (!TOKENS_DIR.exists()) TOKENS_DIR.mkdirs()

    val path = Paths.get(gmailConfig.clientSecretPath) // must be absolute (or you can enforce it)

    if (!path.isAbsolute)
      throw new IllegalStateException(s"${gmailConfig.clientSecretPath} is not an absolute path")

    if (!Files.exists(path))
      throw new IllegalStateException(s"${gmailConfig.clientSecretPath} not found on filesystem")

    val in = Files.newInputStream(path)

    try {
      val clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in, StandardCharsets.UTF_8))
      val flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport,
        JSON_FACTORY,
        clientSecrets,
        java.util.List.of(GmailScopes.GMAIL_SEND)
      )
        .setDataStoreFactory(new FileDataStoreFactory(TOKENS_DIR))
        .setAccessType("offline")
        .build()

      val receiver = new LocalServerReceiver.Builder().setPort(8888).build()
      val credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user")

      new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
        .setApplicationName("scala-gmail-sender")
        .build()
    } finally {
      in.close()
    }
  }

  private def toRawMessage(to: String, subject: String, htmlBody: String): Message = {
    val props = new Properties()
    val session = Session.getInstance(props)
    val email = new MimeMessage(session)

    email.setRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to))
    email.setSubject(subject, "UTF-8")

    email.setContent(htmlBody, "text/html; charset=UTF-8")

    val baos = new java.io.ByteArrayOutputStream()
    email.writeTo(baos)

    val encoded = Base64.getUrlEncoder.withoutPadding().encodeToString(baos.toByteArray)
    new Message().setRaw(encoded)
  }

  def send(to: String, subject: String, bodyText: String): String = {
    val msg = toRawMessage(to, subject, bodyText)
    val sent = SERVICE.users().messages().send("me", msg).execute()
    sent.getId
  }

}
