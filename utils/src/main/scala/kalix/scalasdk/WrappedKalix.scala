package kalix.scalasdk

import kalix.javasdk.OpenKalixRunner

case class WrappedKalix(kalix: Kalix) {

  def createRunner(): OpenKalixRunner = {
    val javaKalix = kalix.delegate
    OpenKalixRunner(javaKalix)
  }

}
