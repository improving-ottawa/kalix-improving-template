package com.improving.extensions.email

import com.comcast.ip4s.{Port, SocketAddress, _}
import org.testcontainers.containers.{BindMode, GenericContainer}
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import com.minosiants.pencil.data._

sealed trait SmtpServerContainer {
  def start(): Unit
  def stop(): Unit
  def smtpPort: Int
  def httpPort: Int
  def socketAddress = SocketAddress(host"localhost", Port.fromInt(smtpPort).get)
  def credentials: Credentials
}

object SmtpServerContainer {
  final private val smtp = 1025
  final private val http = 8025

  def create(): SmtpServerContainer = {
    val username    = Username("pencil")
    val password    = Password("pencil1234")
    val credentials = Credentials(username, password)

    val container = new GenericContainer(DockerImageName.parse("axllent/mailpit"))
    container.withClasspathResourceMapping("container", "/data", BindMode.READ_ONLY)
    container.addExposedPorts(smtp, http)
    container.addEnv("MP_SMTP_AUTH_FILE", "/data/pass.txt")
    container.addEnv("MP_SMTP_TLS_CERT", "/data/certificate.crt")
    container.addEnv("MP_SMTP_TLS_KEY", "/data/keyfile.key")
    container.addEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "true")

    Impl(container, credentials)
  }

  private case class Impl(container: GenericContainer[_], credentials: Credentials) extends SmtpServerContainer {

    def start(): Unit = {
      container.start()
      container.waitingFor(Wait.forListeningPort())
      println(s"SMTP server HTTP service port:$httpPort")
    }

    def stop(): Unit = container.stop()

    def smtpPort: Int = container.getMappedPort(smtp)
    def httpPort: Int = container.getMappedPort(http)
  }

}
