package com.example.gateway

import com.improving.iam._
import com.improving.config._
import com.improving.extensions.oidc._
import com.improving.testkit._
import com.improving.utils._
import com.example.gateway.utils._

import cats.effect._
import cats.effect.unsafe.IORuntime
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pdi.jwt.JwtAlgorithm
import sttp.client3._
import sttp.model.Uri

import scala.concurrent.duration._

/*
 * A manual test of the OIDC based identity service, which can be run locally against a Keycloak Docker instance.
 *
 * @note This is *not* a test specification! Running this test requires a _lot_ of setup!
 * @note This __cannot__ be adapted to run in Github as part of an automated test suite (so don't ask!)
 *
 * @see `README.md` for instructions on how to prepare your local environment to run this test.
 */

/** Test configuration */
object OIDCIdentityTest extends OIDCIdentityTest with IOApp {
  // Change this if you are testing from the UI vs just testing the API
  private val testingFromBrowser = true

  // We test against a local instance of RedHat's Keycloak identity server. It must be setup according to the
  // instructions in the corresponding `Test-Setup-Instructions.md`
  private val keycloakProvider: OIDCClientConfig = {
    val baseKeysPath = "com.example.gateway.identity.providers.local_keycloak"

    val config = ConfigLoader
      .loadOptionalFileSystemConfig("gateway/src/user-local.conf", includeDefaultConfig = false)
      .fold(throw _, identity)

    if (config.hasPath(baseKeysPath)) {
      OIDCClientConfig(
        clientId = config.getString(baseKeysPath + ".client-id"),
        clientSecret = config.getString(baseKeysPath + ".client-secret"),
        discoveryUri = config.getString(baseKeysPath + ".discovery-uri")
      )
    } else {
      throw new RuntimeException("You have not configured your `user-local.conf`! Follow the setup instructions!")
    }
  }

  // DO NOT change these
  private val localPrefix: String = "http://localhost:9000"

  private val callbackUri =
    if (testingFromBrowser) "http://localhost:3000/oidc/callback"
    else s"$localPrefix/oidc/callback"

  private val issuerUri = "http://localhost:8010"

  // The `KeyLoader` configuration, which will load the test ECDSA public/private keypair contained in `resources`.
  // Should not need to change this!
  private val keyLoaderConfig = KeyLoaderConfig(
    jwtAlgorithm = JwtAlgorithm.ES256,
    publicKeyFilePath = "resource:/ec_test_pub.pem",
    privateKeyFilePath = "resource:/ec_test_key.pem",
    privateKeyPassword = Some("test")
  )

  private val identityServiceConfig = OIDCIdentityServiceConfig(
    // Assuming no changes to `docker-compose.yml`
    providerCallback = Uri.unsafeParse(callbackUri),
    // Single registered OIDC provider for local Keycloak running in Docker
    providers = Map("local_keycloak" -> keycloakProvider)
  )

  private val jwtIssuerConfig = JwtIssuerConfig(
    // Note: this will need to change if you change your Kalix proxy configuration!
    tokenIssuerUrl = issuerUri,
    tokenValidDuration = FiniteDuration(1, "hour"),
    defaultUserRole = "Test"
  )

  /* Early exit error */
  private case class EarlyExit(code: Int) extends Error

  /* IOApp Stuff */

  override protected def runtime: IORuntime = asyncContext.catsEffectRuntime

}

/** Actual test */
sealed abstract class OIDCIdentityTest { self: OIDCIdentityTest.type =>

  private val log = Slf4jFactory[IO].getLoggerFromName("ManualTest")

  private val serviceManager = StandAloneServiceManager()

  implicit protected lazy val asyncContext: AsyncContext = AsyncContext.catsEffect()

  private def abortTest(reason: String, cause: Option[Throwable] = None) = {
    val logEffect = cause match {
      case Some(error) => log.error(error)(reason)
      case None        => log.error(s"Aborting test due to: $reason")
    }

    logEffect *> IO.raiseError(EarlyExit(1))
  }

  /** Entrypoint for the Test */
  def run(args: List[String]): IO[ExitCode] = {
    val keycloakEndpointUri = Uri.unsafeParse(keycloakProvider.discoveryUri).withPath("")

    val beginAuthUri = {
      val base        = Uri.unsafeParse(localPrefix)
      val redirectUri = base.addPath("oidc", "check")
      base
        .withPath("oidc", "auth")
        .addQuerySegment(Uri.QuerySegment.KeyValue("provider_id", identityServiceConfig.providers.head._1))
        .addQuerySegment(Uri.QuerySegment.KeyValue("redirect_uri", redirectUri.toString))
        .toString
    }

    val testProgram =
      for {
        _            <- checkForKeycloak(keycloakEndpointUri.toString)
        _            <- log.info("Keycloak found, starting Gateway service (and Kalix proxy)...")
        kalixService <- tryCreateGateway
        _            <- tryStartGateway(kalixService)
        _            <- log.info(s"Service started, to begin test, navigate to: $beginAuthUri")
        _            <- log.info("Press [Enter] to stop service.")
        _            <- cats.effect.std.Console[IO].readLine
      } yield ExitCode.Success

    val adaptedProgram =
      testProgram.handleErrorWith {
        case EarlyExit(code) => IO.pure(ExitCode(code))
        case unhandled       => IO.raiseError(unhandled)
      }

    Resource
      .make(IO.pure(serviceManager))(svcManager => IO(serviceManager.stopServices()))
      .use(_ => adaptedProgram)
  }

  private val checkForKeycloak: String => IO[Unit] =
    discoveryUri => {
      val request            = basicRequest.get(uri"$discoveryUri")
      val sttpBackendOptions = SttpBackendOptions.Default.connectionTimeout(FiniteDuration(3, "seconds"))
      val backend            = HttpClientSyncBackend(sttpBackendOptions)

      IO(backend.send(request).body)
        .flatMap {
          case Right(_)    => IO.unit
          case Left(error) => abortTest(s"Contacted local Keycloak, but received an unexpected response: $error")
        }
        .handleErrorWith { error =>
          abortTest("Could not contact Keycloak due to an error", Some(error))
        }
    }

  private val tryCreateGateway: IO[KalixService] = IO.delay {
    log.info("Creating `gateway` service Kalix instance...")
    val kalixProxyPort = Uri.unsafeParse(localPrefix).authority.flatMap(_.port).getOrElse(9000)
    val kalix          = com.example.gateway.Main.createKalix(keyLoaderConfig, identityServiceConfig, jwtIssuerConfig)
    KalixService(
      serviceName = "gateway",
      kalix,
      overrideProxyPort = Some(kalixProxyPort)
    )
  }

  private val tryStartGateway: KalixService => IO[Unit] =
    kalixService =>
      IO {
        serviceManager.registerKalixService(kalixService)
        serviceManager.startServices()
      }

}
