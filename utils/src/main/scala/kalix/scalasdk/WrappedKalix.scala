package kalix.scalasdk

import com.typesafe.config.Config
import kalix.javasdk.OpenKalixRunner

case class WrappedKalix(kalix: Kalix) {

  def createRunner(configOverride: Option[Config] = None): OpenKalixRunner = {
    val javaKalix = kalix.delegate
    OpenKalixRunner(javaKalix, configOverride)
  }

}
