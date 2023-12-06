package com.improving.extensions.oidc.impl

// Generated from ScalaPB protobuf, contains all the "protobuf stuff" necessary for Akka/Kalix

@SerialVersionUID(0L)
final case class SessionData(
  data: _root_.com.google.protobuf.ByteString = _root_.com.google.protobuf.ByteString.EMPTY,
  unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
) extends scalapb.GeneratedMessage
    with scalapb.lenses.Updatable[SessionData] {

  @transient
  private[this] var __serializedSizeMemoized: _root_.scala.Int = 0

  private[this] def __computeSerializedSize(): _root_.scala.Int = {
    var __size = 0

    {
      val __value = data
      if (!__value.isEmpty) {
        __size += _root_.com.google.protobuf.CodedOutputStream.computeBytesSize(2, __value)
      }
    };
    __size += unknownFields.serializedSize
    __size
  }

  override def serializedSize: _root_.scala.Int = {
    var __size = __serializedSizeMemoized
    if (__size == 0) {
      __size = __computeSerializedSize() + 1
      __serializedSizeMemoized = __size
    }
    __size - 1

  }

  def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
    {
      val __v = data
      if (!__v.isEmpty) {
        _output__.writeBytes(2, __v)
      }
    };
    unknownFields.writeTo(_output__)
  }

  def withData(__v: _root_.com.google.protobuf.ByteString): SessionData = copy(data = __v)

  def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet): SessionData = copy(unknownFields = __v)

  def discardUnknownFields: SessionData = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)

  def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
    (__fieldNumber: @ _root_.scala.unchecked) match {
      case 2 => {
        val __t = data
        if (__t != _root_.com.google.protobuf.ByteString.EMPTY) __t else null
      }
    }
  }

  def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
    _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
    (__field.number: @ _root_.scala.unchecked) match {
      case 2 => _root_.scalapb.descriptors.PByteString(data)
    }
  }

  def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)

  def companion: com.improving.extensions.oidc.impl.SessionData.type = com.improving.extensions.oidc.impl.SessionData
}

object SessionData extends scalapb.GeneratedMessageCompanion[com.improving.extensions.oidc.impl.SessionData] {

  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.improving.extensions.oidc.impl.SessionData] =
    this

  def parseFrom(
    `_input__`: _root_.com.google.protobuf.CodedInputStream
  ): com.improving.extensions.oidc.impl.SessionData = {
    var __data: _root_.com.google.protobuf.ByteString               = _root_.com.google.protobuf.ByteString.EMPTY
    var `_unknownFields__` : _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__                                                     = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0   => _done__ = true
        case 18  =>
          __data = _input__.readBytes()
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    com.improving.extensions.oidc.impl.SessionData(
      data = __data,
      unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }

  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.improving.extensions.oidc.impl.SessionData] =
    _root_.scalapb.descriptors.Reads {
      case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
        _root_.scala.Predef.require(
          __fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor),
          "FieldDescriptor does not match message type."
        )
        com.improving.extensions.oidc.impl.SessionData(
          data = __fieldsMap
            .get(scalaDescriptor.findFieldByNumber(2).get)
            .map(_.as[_root_.com.google.protobuf.ByteString])
            .getOrElse(_root_.com.google.protobuf.ByteString.EMPTY)
        )
      case _                                                => throw new RuntimeException("Expected PMessage")
    }

  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor =
    ReplicatedSessionStoreProto.javaDescriptor.getMessageTypes.get(0)

  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ReplicatedSessionStoreProto.scalaDescriptor.messages(0)

  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] =
    throw new MatchError(__number)

  lazy val nestedMessagesCompanions
    : Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty

  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] =
    throw new MatchError(__fieldNumber)

  lazy val defaultInstance: SessionData = com.improving.extensions.oidc.impl.SessionData(
    data = _root_.com.google.protobuf.ByteString.EMPTY
  )

  implicit class SessionDataLens[UpperPB](
    _l: _root_.scalapb.lenses.Lens[UpperPB, com.improving.extensions.oidc.impl.SessionData]
  ) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.improving.extensions.oidc.impl.SessionData](_l) {

    def data: _root_.scalapb.lenses.Lens[UpperPB, _root_.com.google.protobuf.ByteString] =
      field(_.data)((c_, f_) => c_.copy(data = f_))

  }

  final val DATA_FIELD_NUMBER = 2

  def of(
    data: _root_.com.google.protobuf.ByteString
  ): _root_.com.improving.extensions.oidc.impl.SessionData = _root_.com.improving.extensions.oidc.impl.SessionData(
    data
  )

}

@SerialVersionUID(0L)
final case class SessionKey(
  key: _root_.scala.Predef.String = "",
  unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
) extends scalapb.GeneratedMessage
    with scalapb.lenses.Updatable[SessionKey] {

  @transient
  private[this] var __serializedSizeMemoized: _root_.scala.Int = 0

  private[this] def __computeSerializedSize(): _root_.scala.Int = {
    var __size = 0

    {
      val __value = key
      if (__value.nonEmpty) {
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
      }
    };
    __size += unknownFields.serializedSize
    __size
  }

  override def serializedSize: _root_.scala.Int = {
    var __size = __serializedSizeMemoized
    if (__size == 0) {
      __size = __computeSerializedSize() + 1
      __serializedSizeMemoized = __size
    }
    __size - 1

  }

  def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
    {
      val __v = key
      if (__v.nonEmpty) {
        _output__.writeString(1, __v)
      }
    };
    unknownFields.writeTo(_output__)
  }

  def withKey(__v: _root_.scala.Predef.String): SessionKey = copy(key = __v)

  def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet): SessionKey = copy(unknownFields = __v)

  def discardUnknownFields: SessionKey = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)

  def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
    (__fieldNumber: @ _root_.scala.unchecked) match {
      case 1 => {
        val __t = key
        if (__t != "") __t else null
      }
    }
  }

  def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
    _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
    (__field.number: @ _root_.scala.unchecked) match {
      case 1 => _root_.scalapb.descriptors.PString(key)
    }
  }

  def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)

  def companion: com.improving.extensions.oidc.impl.SessionKey.type = com.improving.extensions.oidc.impl.SessionKey
}

object SessionKey extends scalapb.GeneratedMessageCompanion[com.improving.extensions.oidc.impl.SessionKey] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.improving.extensions.oidc.impl.SessionKey] = this

  def parseFrom(
    `_input__`: _root_.com.google.protobuf.CodedInputStream
  ): com.improving.extensions.oidc.impl.SessionKey = {
    var __key: _root_.scala.Predef.String                           = ""
    var `_unknownFields__` : _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__                                                     = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0   => _done__ = true
        case 10  =>
          __key = _input__.readStringRequireUtf8()
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    com.improving.extensions.oidc.impl.SessionKey(
      key = __key,
      unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }

  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.improving.extensions.oidc.impl.SessionKey] =
    _root_.scalapb.descriptors.Reads {
      case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
        _root_.scala.Predef.require(
          __fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor),
          "FieldDescriptor does not match message type."
        )
        com.improving.extensions.oidc.impl.SessionKey(
          key = __fieldsMap
            .get(scalaDescriptor.findFieldByNumber(1).get)
            .map(_.as[_root_.scala.Predef.String])
            .getOrElse("")
        )
      case _                                                => throw new RuntimeException("Expected PMessage")
    }

  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor =
    ReplicatedSessionStoreProto.javaDescriptor.getMessageTypes.get(1)

  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ReplicatedSessionStoreProto.scalaDescriptor.messages(1)

  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] =
    throw new MatchError(__number)

  lazy val nestedMessagesCompanions
    : Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty

  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] =
    throw new MatchError(__fieldNumber)

  lazy val defaultInstance: SessionKey = com.improving.extensions.oidc.impl.SessionKey(
    key = ""
  )

  implicit class SessionKeyLens[UpperPB](
    _l: _root_.scalapb.lenses.Lens[UpperPB, com.improving.extensions.oidc.impl.SessionKey]
  ) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.improving.extensions.oidc.impl.SessionKey](_l) {

    def key: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] =
      field(_.key)((c_, f_) => c_.copy(key = f_))

  }

  final val KEY_FIELD_NUMBER = 1

  def of(
    key: _root_.scala.Predef.String
  ): _root_.com.improving.extensions.oidc.impl.SessionKey = _root_.com.improving.extensions.oidc.impl.SessionKey(
    key
  )

}

@SerialVersionUID(0L)
final case class StoreSessionRequest(
  key: _root_.scala.Predef.String = "",
  data: _root_.com.google.protobuf.ByteString = _root_.com.google.protobuf.ByteString.EMPTY,
  unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
) extends scalapb.GeneratedMessage
    with scalapb.lenses.Updatable[StoreSessionRequest] {

  @transient
  private[this] var __serializedSizeMemoized: _root_.scala.Int = 0

  private[this] def __computeSerializedSize(): _root_.scala.Int = {
    var __size = 0

    {
      val __value = key
      if (__value.nonEmpty) {
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
      }
    };

    {
      val __value = data
      if (!__value.isEmpty) {
        __size += _root_.com.google.protobuf.CodedOutputStream.computeBytesSize(2, __value)
      }
    };
    __size += unknownFields.serializedSize
    __size
  }

  override def serializedSize: _root_.scala.Int = {
    var __size = __serializedSizeMemoized
    if (__size == 0) {
      __size = __computeSerializedSize() + 1
      __serializedSizeMemoized = __size
    }
    __size - 1

  }

  def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
    {
      val __v = key
      if (__v.nonEmpty) {
        _output__.writeString(1, __v)
      }
    };
    {
      val __v = data
      if (!__v.isEmpty) {
        _output__.writeBytes(2, __v)
      }
    };
    unknownFields.writeTo(_output__)
  }

  def withKey(__v: _root_.scala.Predef.String): StoreSessionRequest = copy(key = __v)

  def withData(__v: _root_.com.google.protobuf.ByteString): StoreSessionRequest = copy(data = __v)

  def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet): StoreSessionRequest = copy(unknownFields = __v)

  def discardUnknownFields: StoreSessionRequest = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)

  def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
    (__fieldNumber: @ _root_.scala.unchecked) match {
      case 1 => {
        val __t = key
        if (__t != "") __t else null
      }
      case 2 => {
        val __t = data
        if (__t != _root_.com.google.protobuf.ByteString.EMPTY) __t else null
      }
    }
  }

  def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
    _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
    (__field.number: @ _root_.scala.unchecked) match {
      case 1 => _root_.scalapb.descriptors.PString(key)
      case 2 => _root_.scalapb.descriptors.PByteString(data)
    }
  }

  def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)

  def companion: com.improving.extensions.oidc.impl.StoreSessionRequest.type =
    com.improving.extensions.oidc.impl.StoreSessionRequest

}

object StoreSessionRequest
    extends scalapb.GeneratedMessageCompanion[com.improving.extensions.oidc.impl.StoreSessionRequest] {

  implicit def messageCompanion
    : scalapb.GeneratedMessageCompanion[com.improving.extensions.oidc.impl.StoreSessionRequest] = this

  def parseFrom(
    `_input__`: _root_.com.google.protobuf.CodedInputStream
  ): com.improving.extensions.oidc.impl.StoreSessionRequest = {
    var __key: _root_.scala.Predef.String                           = ""
    var __data: _root_.com.google.protobuf.ByteString               = _root_.com.google.protobuf.ByteString.EMPTY
    var `_unknownFields__` : _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__                                                     = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0   => _done__ = true
        case 10  =>
          __key = _input__.readStringRequireUtf8()
        case 18  =>
          __data = _input__.readBytes()
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    com.improving.extensions.oidc.impl.StoreSessionRequest(
      key = __key,
      data = __data,
      unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }

  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.improving.extensions.oidc.impl.StoreSessionRequest] =
    _root_.scalapb.descriptors.Reads {
      case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
        _root_.scala.Predef.require(
          __fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor),
          "FieldDescriptor does not match message type."
        )
        com.improving.extensions.oidc.impl.StoreSessionRequest(
          key = __fieldsMap
            .get(scalaDescriptor.findFieldByNumber(1).get)
            .map(_.as[_root_.scala.Predef.String])
            .getOrElse(""),
          data = __fieldsMap
            .get(scalaDescriptor.findFieldByNumber(2).get)
            .map(_.as[_root_.com.google.protobuf.ByteString])
            .getOrElse(_root_.com.google.protobuf.ByteString.EMPTY)
        )
      case _                                                => throw new RuntimeException("Expected PMessage")
    }

  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor =
    ReplicatedSessionStoreProto.javaDescriptor.getMessageTypes.get(2)

  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ReplicatedSessionStoreProto.scalaDescriptor.messages(2)

  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] =
    throw new MatchError(__number)

  lazy val nestedMessagesCompanions
    : Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty

  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] =
    throw new MatchError(__fieldNumber)

  lazy val defaultInstance: StoreSessionRequest = com.improving.extensions.oidc.impl.StoreSessionRequest(
    key = "",
    data = _root_.com.google.protobuf.ByteString.EMPTY
  )

  implicit class StoreSessionRequestLens[UpperPB](
    _l: _root_.scalapb.lenses.Lens[UpperPB, com.improving.extensions.oidc.impl.StoreSessionRequest]
  ) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.improving.extensions.oidc.impl.StoreSessionRequest](_l) {

    def key: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] =
      field(_.key)((c_, f_) => c_.copy(key = f_))

    def data: _root_.scalapb.lenses.Lens[UpperPB, _root_.com.google.protobuf.ByteString] =
      field(_.data)((c_, f_) => c_.copy(data = f_))

  }

  final val KEY_FIELD_NUMBER  = 1
  final val DATA_FIELD_NUMBER = 2

  def of(
    key: _root_.scala.Predef.String,
    data: _root_.com.google.protobuf.ByteString
  ): _root_.com.improving.extensions.oidc.impl.StoreSessionRequest =
    _root_.com.improving.extensions.oidc.impl.StoreSessionRequest(
      key,
      data
    )

}

@SerialVersionUID(0L)
final case class GetSessionResponse(
  session: _root_.scala.Option[com.improving.extensions.oidc.impl.SessionData] = _root_.scala.None,
  unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
) extends scalapb.GeneratedMessage
    with scalapb.lenses.Updatable[GetSessionResponse] {

  @transient
  private[this] var __serializedSizeMemoized: _root_.scala.Int = 0

  private[this] def __computeSerializedSize(): _root_.scala.Int = {
    var __size = 0
    if (session.isDefined) {
      val __value = session.get
      __size += 1 + _root_.com.google.protobuf.CodedOutputStream
        .computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
    };
    __size += unknownFields.serializedSize
    __size
  }

  override def serializedSize: _root_.scala.Int = {
    var __size = __serializedSizeMemoized
    if (__size == 0) {
      __size = __computeSerializedSize() + 1
      __serializedSizeMemoized = __size
    }
    __size - 1

  }

  def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
    session.foreach { __v =>
      val __m = __v
      _output__.writeTag(1, 2)
      _output__.writeUInt32NoTag(__m.serializedSize)
      __m.writeTo(_output__)
    };
    unknownFields.writeTo(_output__)
  }

  def getSession: com.improving.extensions.oidc.impl.SessionData =
    session.getOrElse(com.improving.extensions.oidc.impl.SessionData.defaultInstance)

  def clearSession: GetSessionResponse = copy(session = _root_.scala.None)

  def withSession(__v: com.improving.extensions.oidc.impl.SessionData): GetSessionResponse = copy(session = Option(__v))

  def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet): GetSessionResponse = copy(unknownFields = __v)

  def discardUnknownFields: GetSessionResponse = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)

  def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
    (__fieldNumber: @ _root_.scala.unchecked) match {
      case 1 => session.orNull
    }
  }

  def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
    _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
    (__field.number: @ _root_.scala.unchecked) match {
      case 1 => session.map(_.toPMessage).getOrElse(_root_.scalapb.descriptors.PEmpty)
    }
  }

  def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)

  def companion: com.improving.extensions.oidc.impl.GetSessionResponse.type =
    com.improving.extensions.oidc.impl.GetSessionResponse

}

object GetSessionResponse
    extends scalapb.GeneratedMessageCompanion[com.improving.extensions.oidc.impl.GetSessionResponse] {

  implicit def messageCompanion
    : scalapb.GeneratedMessageCompanion[com.improving.extensions.oidc.impl.GetSessionResponse] = this

  def parseFrom(
    `_input__`: _root_.com.google.protobuf.CodedInputStream
  ): com.improving.extensions.oidc.impl.GetSessionResponse = {
    var __session: _root_.scala.Option[com.improving.extensions.oidc.impl.SessionData] = _root_.scala.None
    var `_unknownFields__` : _root_.scalapb.UnknownFieldSet.Builder                    = null
    var _done__                                                                        = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0   => _done__ = true
        case 10  =>
          __session = Option(
            __session.fold(
              _root_.scalapb.LiteParser.readMessage[com.improving.extensions.oidc.impl.SessionData](_input__)
            )(_root_.scalapb.LiteParser.readMessage(_input__, _))
          )
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    com.improving.extensions.oidc.impl.GetSessionResponse(
      session = __session,
      unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }

  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.improving.extensions.oidc.impl.GetSessionResponse] =
    _root_.scalapb.descriptors.Reads {
      case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
        _root_.scala.Predef.require(
          __fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor),
          "FieldDescriptor does not match message type."
        )
        com.improving.extensions.oidc.impl.GetSessionResponse(
          session = __fieldsMap
            .get(scalaDescriptor.findFieldByNumber(1).get)
            .flatMap(_.as[_root_.scala.Option[com.improving.extensions.oidc.impl.SessionData]])
        )
      case _                                                => throw new RuntimeException("Expected PMessage")
    }

  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor =
    ReplicatedSessionStoreProto.javaDescriptor.getMessageTypes().get(3)

  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ReplicatedSessionStoreProto.scalaDescriptor.messages(3)

  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @ _root_.scala.unchecked) match {
      case 1 => __out = com.improving.extensions.oidc.impl.SessionData
    }
    __out
  }

  lazy val nestedMessagesCompanions
    : Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty

  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] =
    throw new MatchError(__fieldNumber)

  lazy val defaultInstance: GetSessionResponse = com.improving.extensions.oidc.impl.GetSessionResponse(
    session = _root_.scala.None
  )

  implicit class GetSessionResponseLens[UpperPB](
    _l: _root_.scalapb.lenses.Lens[UpperPB, com.improving.extensions.oidc.impl.GetSessionResponse]
  ) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.improving.extensions.oidc.impl.GetSessionResponse](_l) {

    def session: _root_.scalapb.lenses.Lens[UpperPB, com.improving.extensions.oidc.impl.SessionData] =
      field(_.getSession)((c_, f_) => c_.copy(session = Option(f_)))

    def optionalSession
      : _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Option[com.improving.extensions.oidc.impl.SessionData]] =
      field(_.session)((c_, f_) => c_.copy(session = f_))

  }

  final val SESSION_FIELD_NUMBER = 1

  def of(
    session: _root_.scala.Option[com.improving.extensions.oidc.impl.SessionData]
  ): _root_.com.improving.extensions.oidc.impl.GetSessionResponse =
    _root_.com.improving.extensions.oidc.impl.GetSessionResponse(
      session
    )

}

object ReplicatedSessionStoreProto extends _root_.scalapb.GeneratedFileObject {

  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    com.google.protobuf.empty.EmptyProto,
    kalix.AnnotationsProto,
    scalapb.options.ScalapbProto
  )

  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      com.improving.extensions.oidc.impl.SessionData,
      com.improving.extensions.oidc.impl.SessionKey,
      com.improving.extensions.oidc.impl.StoreSessionRequest,
      com.improving.extensions.oidc.impl.GetSessionResponse
    )

  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
    scalapb.Encoding.fromBase64(
      scala.collection.immutable
        .Seq(
          """CkFjb20vZXhhbXBsZS9ib3VuZGVkLWNvbnRleHQvZW50aXR5L3JlcGxpY2F0ZWRfc2Vzc2lvbl9zdG9yZS5wcm90bxIiY29tL
  mltcHJvdmluZy5leHRlbnNpb25zLm9pZGMuaW1wbBobZ29vZ2xlL3Byb3RvYnVmL2VtcHR5LnByb3RvGhdrYWxpeC9hbm5vdGF0a
  W9ucy5wcm90bxoVc2NhbGFwYi9zY2FsYXBiLnByb3RvIiwKC1Nlc3Npb25EYXRhEh0KBGRhdGEYAiABKAxCCeI/BhIEZGF0YVIEZ
  GF0YSItCgpTZXNzaW9uS2V5Eh8KA2tleRgBIAEoCUIN4j8FEgNrZXnCQwIIAVIDa2V5IlUKE1N0b3JlU2Vzc2lvblJlcXVlc3QSH
  woDa2V5GAEgASgJQg3iPwUSA2tlecJDAggBUgNrZXkSHQoEZGF0YRgCIAEoDEIJ4j8GEgRkYXRhUgRkYXRhIm0KEkdldFNlc3Npb
  25SZXNwb25zZRJXCgdzZXNzaW9uGAEgASgLMi8uY29tLmltcHJvdmluZy5leHRlbnNpb25zLm9pZGMuaW1wbC5TZXNzaW9uRGF0Y
  UIM4j8JEgdzZXNzaW9uUgdzZXNzaW9uMowDChZSZXBsaWNhdGVkU2Vzc2lvblN0b3JlEl0KClB1dFNlc3Npb24SNy5jb20uaW1wc
  m92aW5nLmV4dGVuc2lvbnMub2lkYy5pbXBsLlN0b3JlU2Vzc2lvblJlcXVlc3QaFi5nb29nbGUucHJvdG9idWYuRW1wdHkSdAoKR
  2V0U2Vzc2lvbhIuLmNvbS5pbXByb3ZpbmcuZXh0ZW5zaW9ucy5vaWRjLmltcGwuU2Vzc2lvbktleRo2LmNvbS5pbXByb3ZpbmcuZ
  Xh0ZW5zaW9ucy5vaWRjLmltcGwuR2V0U2Vzc2lvblJlc3BvbnNlGpwBykOYARqVAQo/Y29tLmltcHJvdmluZy5leHRlbnNpb25zL
  m9pZGMuaW1wbC5SZXBsaWNhdGVkU2Vzc2lvblN0b3JlRW50aXR5EhhyZXBsaWNhdGVkLXNlc3Npb24tc3RvcmVCOAoGc3RyaW5nE
  i5jb20uaW1wcm92aW5nLmV4dGVuc2lvbnMub2lkYy5pbXBsLlNlc3Npb25EYXRhQjDiPy0KImNvbS5pbXByb3ZpbmcuZXh0ZW5za
  W9ucy5vaWRjLmltcGwQASgBWAC4AQBiBnByb3RvMw=="""
        )
        .mkString
    )

  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }

  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(
      javaProto,
      _root_.scala.Array(
        com.google.protobuf.empty.EmptyProto.javaDescriptor,
        kalix.AnnotationsProto.javaDescriptor,
        scalapb.options.ScalapbProto.javaDescriptor
      )
    )
  }

  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor

}
