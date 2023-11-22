package com.improving

import kalix.scalasdk.{testkit => kalixTestkit}

package object testkit {

  final type EventingSupport = kalixTestkit.KalixTestKit.Settings.EventingSupport

  object EventingSupport {

    /**
     * This is the default type used and allows the testing eventing integrations without an external broker dependency
     * running.
     */
    final val TestBroker: EventingSupport = kalixTestkit.KalixTestKit.Settings.TestBroker

    /**
     * Used if you want to use an external Google PubSub (or its Emulator) on your tests.
     *
     * Note: the Google PubSub broker instance needs to be started independently.
     */
    final val GooglePubSub = kalixTestkit.KalixTestKit.Settings.GooglePubSub

    /**
     * Used if you want to use an external Kafka broker on your tests.
     *
     * Note: the Kafka broker instance needs to be started independently.
     */
    final val Kafka = kalixTestkit.KalixTestKit.Settings.Kafka

  }

  final class IntegrationTestError private[testkit](message: String, cause: Option[Throwable] = None)
    extends Error(message, cause.orNull)

}
