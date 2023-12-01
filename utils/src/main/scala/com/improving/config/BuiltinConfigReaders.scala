package com.improving.config

import scala.jdk.CollectionConverters._
import scala.util.Try

abstract private[config] class BuiltinConfigReaders { self: readers.type =>
  import BuiltinConfigReaders._
  import ConfigReader.pathInputReader
  import scala.jdk.CollectionConverters._

  final val hasPath    = pathInputReader(path => cfg => cfg.hasPath(path))
  final val pathIsNull = pathInputReader(path => cfg => cfg.getIsNull(path))

  final val getBoolean   = pathInputReader(path => cfg => cfg.getBoolean(path))
  final val getInt       = pathInputReader(path => cfg => cfg.getInt(path))
  final val getLong      = pathInputReader(path => cfg => cfg.getLong(path))
  final val getDouble    = pathInputReader(path => cfg => cfg.getDouble(path))
  final val getString    = pathInputReader(path => cfg => cfg.getString(path))
  final val getStringOpt = pathInputReader(path => cfg => Try(cfg.getString(path)).toOption)

  final val getConfigObject = pathInputReader(path => cfg => cfg.getObject(path))
  final val getConfig       = pathInputReader(path => cfg => cfg.getConfig(path))
  final val getConfigValue  = pathInputReader(path => cfg => cfg.getValue(path))

  final val getBytesSize  = pathInputReader(path => cfg => Long.unbox(cfg.getBytes(path)))
  final val getMemorySize = pathInputReader(path => cfg => cfg.getMemorySize(path))

  final val getJavaDuration   = pathInputReader(path => cfg => cfg.getDuration(path))
  final val getFiniteDuration = pathInputReader(path => cfg => cfg.getFiniteDuration(path))
  final val getBase64Bytes    = pathInputReader(path => cfg => decode64(cfg.getString(path)))
  final val getFileSystemPath = pathInputReader(path => cfg => cfg.getString(path)).andThen(_.map(asPath))

  final val getBooleanList = pathInputReader(path => cfg => cfg.getBooleanList(path).asScala.map(Boolean.unbox).toList)
  final val getIntList     = pathInputReader(path => cfg => cfg.getIntList(path).asScala.map(Int.unbox).toList)
  final val getLongList    = pathInputReader(path => cfg => cfg.getLongList(path).asScala.map(Long.unbox).toList)
  final val getDoubleList  = pathInputReader(path => cfg => cfg.getDoubleList(path).asScala.map(Double.unbox).toList)
  final val getStringList  = pathInputReader(path => cfg => cfg.getStringList(path).asScala.toList)
  final val getConfigList  = pathInputReader(path => cfg => cfg.getConfigList(path).asScala.toList)

  final val getStringsMap = pathInputReader { path =>
    cfg => cfg.getObject(path).unwrapped().asScala.view.mapValues(_.asInstanceOf[String]).toMap
  }
}

private object BuiltinConfigReaders {
  /*
   * Private/internal functions
   */
  @inline final private def decode64(string: String): Array[Byte] = java.util.Base64.getDecoder.decode(string)

  @inline final private def asPath(path: String) = java.nio.file.Path.of(path).toAbsolutePath
}
