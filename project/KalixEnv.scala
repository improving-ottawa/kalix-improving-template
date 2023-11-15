import scala.sys.process.*

import sbt.{Def, _}
import sbt.Keys.*

import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.*

object KalixEnv {
  /* Kalix Configuration */

  /** The Docker/containerd image repository used when deploying Kalix service images */
  val containerRepository = "kcr.us-east-1.kalix.io"

  /** The Kalix organization name of this Kalix project */
  val organizationName = "improving"

  /** The Kalix project name of this Kalix project */
  val projectName = "kalix-improving-template"

  /** The Kalix secrets path to the JWT private key password */
  val jwtSecretPrivateKeyPath = "private-key-password/password"

  /** The Kalix project ID of this Kalix project */
  val projectId = "???"

  /**
   * A map of Kalix secrets to environment variables (keys are environment variables and values are the kalix secret
   * path).
   */
  val secretsMap: Map[String, String] = Map(
    "JWT_KEY_PASSWORD" -> "private-key-password/password",
    // TODO: uncomment if using a mail client
    // "MAILJET_API_KEY" -> "mailjet-secret/APIKEY",
    // "MAILJET_PRIVATE_KEY" -> "mailjet-secret/PRIVATEKEY",
  )

  /* Sbt tasks */

  val publishContainers =
    taskKey[Unit]("Publishes all Kalix service containers to the configured container repository.")

  val deployServices = taskKey[Unit]("Deploys all Kalix services using the latest version.")

  val publishAndDeploy = taskKey[Unit](
    "Builds Kalix service images, publishes them to the container registry," +
      "then deploys all Kalix services to the Kalix cloud."
  )

  /* Utility / helper function(s) */

  private def runConsoleCommand(cmd: String): Unit = {
    val exitCode = cmd.!
    if (exitCode != 0) {
      throw new RuntimeException(s"Command failed: $cmd")
    }
  }

  private def authenicateKalix(): Unit = {
    val kalixTokenOption = Option(System.getenv("KALIX_TOKEN"))
    // If we found a token, use it to login to Kalix and configure docker auth
    kalixTokenOption match {
      case None             => throw new RuntimeException("KALIX_TOKEN environment variable not available.")
      case Some(kalixToken) =>
        // Authenticate against Kalix
        runConsoleCommand(s"kalix auth use-token $kalixToken --disable-prompt -q")
        // Set the current project
        runConsoleCommand(s"kalix config set project $projectId --disable-prompt -q")
    }
  }

  private def configureDockerForKalix(): Unit = {
    // Authenticate against Kalix and set the project
    authenicateKalix()

    // Configure docker
    runConsoleCommand("kalix auth container-registry configure --disable-prompt -q")
  }

  /* Public "publish" Sbt functions (tasks) */

  /** Publishes all container images for the specified Kalix service projects. */
  def publishProjectContainers(allProjects: Seq[Project]): Def.Initialize[Task[Unit]] =
    Def.taskDyn {
      // Configure the Kalix project environment for `docker push`
      configureDockerForKalix()

      // Build and publish the container image for each service
      Def.sequential(
        allProjects.map { project =>
          Def.taskDyn {
            val dockerAliasNames = (project / dockerAliases).value.map(_.toString)
            val projName = (project / name).value
            val logger = (project / streams).value.log
            logger.info(s"Publishing image for $projectName / $projName with the following tags:")
            dockerAliasNames.foreach(alias => logger.info(s"\t- $alias"))

            project / Docker / publish
          }
        }
      )

    }

  private def formatSecretsEnv(secretsMap: Map[String, String]): String = {
    if (secretsMap.isEmpty) ""
    else {
      val mappings = secretsMap.map { case (key, value) => s"$key=$value" }.mkString(",")
      s"--secret-env $mappings"
    }
  }

  def deployProjectServices(allProjects: Seq[Project]): Def.Initialize[Task[Unit]] =
    Def.taskDyn {
      // Check for Kalix token and authenticate via `kalix` cli.
      authenicateKalix()

      // Deploy each Kalix service to the Kalix cloud
      Def.sequential(
        allProjects.map { pubProject =>
          Def.taskDyn {
            val dockerAliasNames = (pubProject / dockerAliases).value.map(_.toString)
            val projName = (pubProject / name).value
            val logger = (pubProject / streams).value.log
            val dynVersionTag = dockerAliasNames.filterNot(_ == "latest").headOption

            dynVersionTag match {
              case None =>
                Def.task(logger.warn(s"No `latest` tag found for project: $projName (skipped deployment)"))

              case Some(publishTag) =>
                Def.task {
                  logger.info(s"Deploying Kalix service: $projName...")
                  val secrets = formatSecretsEnv(secretsMap)
                  // Deploy the service (or update the service in case it is not already deployed)
                  runConsoleCommand(s"kalix service deploy $projName $publishTag $secrets -q --project $projectId")
                  logger.info("Kalix service published successfully")
                }
            }
          }
        }
      )

    }

}
