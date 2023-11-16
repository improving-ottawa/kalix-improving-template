package com.example.common.domain

import com.example.common.domain.address.{Address, EditableAddress, PhoneNumber}
import scalapb.GeneratedMessage
import scalapb.json4s.JsonFormat

import scala.language.implicitConversions
import scala.util.Try

object util {

  implicit class PhoneNumberUtil(phoneNumber: PhoneNumber) {

    implicit def printPhoneNumber: String =
      s"${phoneNumber.countryCode}-(${phoneNumber.areaCode})-${phoneNumber.personalNumber.splitAt(3).productIterator.toSeq.mkString("-")}"

  }

  implicit class AddressUtil(address: Address) {

    implicit def toEditable: EditableAddress = EditableAddress(
      line1 = Some(address.line1),
      line2 = address.line2,
      city = Some(address.city),
      stateProvince = Some(address.stateProvince),
      country = Some(address.country),
      postalCode = address.postalCode
    )

    implicit def updateAddress(editable: EditableAddress): Address = Address(
      line1 = editable.line1.getOrElse(address.line1),
      line2 = editable.line2.orElse(address.line2),
      city = editable.city.getOrElse(address.country),
      stateProvince = editable.stateProvince.getOrElse(address.stateProvince),
      country = editable.country.getOrElse(address.country),
      postalCode = editable.postalCode.orElse(address.postalCode)
    )

  }

  implicit class EditableAddressUtil(address: EditableAddress) {

    implicit def copyFromEditable(oldAddress: Address): Address =
      Address(
        line1 = address.line1.getOrElse(oldAddress.line1),
        line2 = address.line2.orElse(oldAddress.line2),
        city = address.city.getOrElse(oldAddress.city),
        stateProvince = address.stateProvince.getOrElse(oldAddress.stateProvince),
        country = address.country.getOrElse(oldAddress.country),
        postalCode = address.postalCode.orElse(oldAddress.postalCode),
      )

    implicit def toAddress: Address = Address(
      line1 = address.getLine1,
      line2 = address.line2,
      city = address.getCity,
      stateProvince = address.getStateProvince,
      country = address.getCountry,
      postalCode = address.postalCode
    )

    implicit def updateAddress(editable: EditableAddress): EditableAddress = EditableAddress(
      line1 = editable.line1.orElse(address.line1),
      line2 = editable.line2.orElse(address.line2),
      city = editable.city.orElse(address.city),
      stateProvince = editable.stateProvince.orElse(address.stateProvince),
      country = editable.country.orElse(address.country),
      postalCode = editable.postalCode.orElse(address.postalCode)
    )

  }

  implicit class GeneratedMessageUtil[T <: GeneratedMessage](req: T) {

    implicit def printAsResponse: String = s"""\"${JsonFormat.toJsonString(req).replace("\"", "\\\"")}\""""

    implicit def printAsDataResponse: String = s"""${JsonFormat
        .toJsonString(req)
        .replace("\\\\\\", "\\")
        .replace("\"\\\"", "")
        .replace("\\\"\"", "")}"""

  }

  implicit class StringUtil(str: String) {

    implicit def parsePhoneNumber: Try[PhoneNumber] = Try(
      PhoneNumber(
        s"${str.take(1)}",
        s"${str.slice(3, 6)}",
        s"${str.slice(8, 11)}${str.slice(12, 16)}"
      )
    )

  }

}
