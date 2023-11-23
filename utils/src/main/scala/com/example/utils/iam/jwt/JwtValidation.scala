package com.example.utils.iam.jwt

import akka.http.scaladsl.model.DateTime

import java.util.UUID
import cats.data._
import cats.implicits._
import com.example.utils.iam.model.IamPayload

import java.time.Instant
import scala.annotation.tailrec

/**
  * A specific JWT validation error.
  */
trait JwtValidationError {
  val errorMessage: String
}

/**
  * Indicates that a specific JWT was successfully validated.
  */
sealed trait JwtValidationSuccess
object JwtValidationSuccess extends JwtValidationSuccess

/**
  * Represents a JWT payload, not specific to any particular specific claim.
  */
final case class JwtPayload(jwt: JsonWebToken[_ <: IamPayload]) extends AnyVal {
  def issuer: String             = jwt.issuer
  def subject: UUID              = jwt.subject
  def audience: Set[String]      = jwt.audience
  def expiration: Instant        = jwt.expiration
  def issuedAt: Instant          = jwt.issuedAt
  def notBefore: Option[Instant] = jwt.notBefore
  def jwtId: UUID                = jwt.jwtId
}

object JwtPayload {
  import scala.language.implicitConversions
  implicit final def payloadFromToken[P <: IamPayload](token: JsonWebToken[P]): JwtPayload = JwtPayload(token)
}

/**
  * A JWT validator, which performs some validation against a [[JsonWebToken]]
  */
sealed trait JwtValidator[-P <: IamPayload] {
  def apply[X <: P](jwt: JsonWebToken[X]): ValidatedNel[JwtValidationError, JwtValidationSuccess]
}

/**
  * Functions used to validate JWTs.
  */
object JwtValidator {
  final type ValidatorResult                    = ValidatedNel[JwtValidationError, JwtValidationSuccess]
  final type GenericValidator                   = JwtValidator[IamPayload]
  final type SpecificValidator[P <: IamPayload] = JwtValidator[P]

  // Types of JwtValidators
  final case class JwtPayloadValidator(validate: JwtPayload => ValidatorResult) extends JwtValidator[IamPayload] {
    def apply[X <: IamPayload](jwt: JsonWebToken[X]) = validate(JwtPayload(jwt))
  }

  final case class JwtClaimValidator[P <: IamPayload](validate: JsonWebToken[P] => ValidatorResult)
      extends JwtValidator[P] {
    def apply[X <: P](jwt: JsonWebToken[X]) = validate(jwt.asInstanceOf[JsonWebToken[P]])
  }

  // Helper functions for defining new validators
  final def validJwt: ValidatorResult                                   = JwtValidationSuccess.validNel
  final def error(validationError: JwtValidationError): ValidatorResult = validationError.invalidNel

  final def checkOrFailWithError(check: Boolean)(failError: => JwtValidationError): ValidatorResult =
    if (check) validJwt else error(failError)

  final def errors(firstError: JwtValidationError, otherErrors: JwtValidationError*): ValidatorResult =
    Validated.Invalid(NonEmptyList.of(firstError, otherErrors: _*))

  // Constructor convenience functions
  final def payload(validator: JwtPayload => ValidatorResult): GenericValidator =
    JwtPayloadValidator(validator)

  final def claim[P <: IamPayload](validator: JsonWebToken[P] => ValidatorResult): SpecificValidator[P] =
    JwtClaimValidator(validator)

}

object JwtValidation {
  import JwtValidator._

  final type ValidationResult[P <: IamPayload] = Either[TokenValidationException, JsonWebToken[P]]

  final case class TokenValidationException(errors: NonEmptyList[JwtValidationError])
      extends Throwable("Token failed one or more validations.")

  final val defaultValidations: List[GenericValidator] =
    List(
      // Validate the expiration and "not before" timestamps
      validators.validateTiming
    )

  /**
    * Validates a [[JsonWebToken]] using the default validators.
    * @param token
    *   The Jwt to validate.
    * @return
    *   Either a [[TokenValidationException]] containing one or more validation errors or the input token.
    */
  final def validateDefault[P <: IamPayload](
    token: JsonWebToken[P]
  ): Either[TokenValidationException, JsonWebToken[P]] = {
    collectErrors(
      token,
      defaultValidations.map(validator => validator(token))
    )
  }

  /**
    * Validates a [[JsonWebToken]] using the provided list of validators.
    * @param token
    *   The Jwt to validate.
    * @param validators
    *   The validators to run against the Jwt.
    * @return
    *   Either a [[TokenValidationException]] containing one or more validation errors or the input token.
    */
  final def validate[P <: IamPayload](
    token: JsonWebToken[P],
    validators: NonEmptyList[JwtValidator[P]]
  ): Either[TokenValidationException, JsonWebToken[P]] =
    collectErrors(
      token,
      validators.map(validator => validator(token)).toList
    )

  /**
    * Simply checks if a [[JsonWebToken]] is expired or not, using the current (server) Utc timestamp.
    * @param token
    *   The token to check.
    * @return
    *   True if the token is expired, false otherwise.
    */
  final def isExpired(token: JsonWebToken[_]): Boolean = token.expiration.isBefore(Instant.now())

  private def failWithErrors(errors: Seq[JwtValidationError]): TokenValidationException =
    TokenValidationException(NonEmptyList.of(errors.head, errors.tail: _*))

  @tailrec
  private def collectErrors[P <: IamPayload](
    token: JsonWebToken[P],
    results: List[ValidatorResult],
    accErrors: Vector[JwtValidationError] = Vector.empty
  ): Either[TokenValidationException, JsonWebToken[P]] = {

    import Validated._

    results match {
      case Invalid(errors) :: tail   => collectErrors(token, tail, accErrors ++ errors.toList)
      case Valid(_) :: tail          => collectErrors(token, tail, accErrors)
      case Nil if accErrors.isEmpty  => Right(token)
      case Nil if accErrors.nonEmpty => Left(failWithErrors(accErrors))
    }
  }

  /** Default errors * */
  object errors {

    final case class JwtNotBeforeError(jwt: JwtPayload, now: Instant) extends JwtValidationError {
      override val errorMessage = s"Token is not valid before ${jwt.notBefore} (now: ${now.toString})"
    }

    final case class JwtExpiredError(jwt: JwtPayload, now: Instant) extends JwtValidationError {
      override val errorMessage = s"Token is only valid until ${jwt.expiration.toString} (now: ${now.toString})"
    }

    final case class JwtInvalidIssuer(jwt: JwtPayload, expectedIssuer: String) extends JwtValidationError {

      override val errorMessage = s"Token was not issued by correct issuer " +
        s"(claimed issuer: ${jwt.issuer}, expected issuer: $expectedIssuer)"

    }

  }

  object validators {
    import errors._

    /**
      * Checks the expiration and "not before" timestamp of the token against the current (server) Utc timestamp.
      */
    final val validateTiming: GenericValidator = JwtValidator.payload { jwt =>
      val now = Instant.now()

      def checkNotBefore: ValidatorResult = jwt.notBefore match {
        case Some(nbf) => checkOrFailWithError(now.isBefore(nbf))(JwtNotBeforeError(jwt, now))
        case None      => validJwt
      }

      def checkExpiration: ValidatorResult =
        checkOrFailWithError(jwt.expiration.isAfter(now))(JwtExpiredError(jwt, now))

      checkNotBefore.andThen(_ => checkExpiration)
    }

  }

}
