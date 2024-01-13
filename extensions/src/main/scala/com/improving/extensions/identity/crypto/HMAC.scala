package com.improving.extensions.identity.crypto

import com.improving.utils.SecureString
import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.macs.{HMac => Mac}
import org.bouncycastle.jcajce.provider.digest.BCMessageDigest
import org.bouncycastle.jcajce.provider.digest
import org.bouncycastle.crypto.params.KeyParameter

import scala.util.Try

/**
 * This class provides the functionality of a "Hash-based Message Authentication Code" (HMAC) algorithm.
 *
 * @note This class is __absolutely not__ thread-safe!
 */
final class HMAC private(instance: Mac) {
  private final val hashSize = instance.getMacSize

  def hash(data: Array[Byte]): Array[Byte] = {
    val output = new Array[Byte](hashSize)
    instance.update(data, 0, data.length)
    instance.doFinal(output, 0)
    output
  }

}

object HMAC {

  // Load the BouncyCastle provider on class instantiation
  com.improving.utils.BouncyCastle.register()

  def create(privateKey: SecureString, digestAlgorithm: String): Try[HMAC] =
    Try(createUnsafe(privateKey, digestAlgorithm))

  def createFromKeyParam(keyParam: KeyParameter, digestAlgorithm: String): Try[HMAC] =
    Try(createFromKeyParamUnsafe(keyParam, digestAlgorithm))

  def createFromKeyParamUnsafe(keyParam: KeyParameter, digestAlgorithm: String): HMAC = {
    val macInstance = new Mac(getDigestByName(digestAlgorithm))
    macInstance.init(keyParam)

    new HMAC(macInstance)
  }

  def createFromDigestUnsafe(privateKey: SecureString, digest: Digest): HMAC = {
    val keyParam = new KeyParameter(privateKey.readOnce())
    val macInstance = new Mac(digest)
    macInstance.init(keyParam)

    new HMAC(macInstance)
  }

  def createUnsafe(privateKey: SecureString, digestAlgorithm: String): HMAC =
    createFromDigestUnsafe(privateKey, getDigestByName(digestAlgorithm))

  /* BouncyCastle Stuff */

  private final type BCDigest = BCMessageDigest with Cloneable

  private final object DummyDigest extends Digest {
    def getAlgorithmName = "Dummy"
    def getDigestSize = 0
    def update(in: Byte): Unit = ()
    def update(in: Array[Byte], inOff: Int, len: Int): Unit = ()
    def doFinal(out: Array[Byte], outOff: Int) = 0
    def reset(): Unit = ()
  }

  private final val bcDigestType = classOf[BCMessageDigest]

  private final val bcDigestField = {
    val field = bcDigestType.getDeclaredField("digest")
    field.setAccessible(true)
    field
  }

  private final class BCDigestUnwrapper[T <: BCDigest](wrapped: T) {
    private def getInnerDigest(bcMessageDigest: BCMessageDigest): Digest = {
      val obj = bcDigestField.get(bcMessageDigest)
      obj.asInstanceOf[Digest]
    }

    def getInstance: Digest = getInnerDigest(wrapped.clone().asInstanceOf[BCMessageDigest])
  }

  private final def makeDigest[T <: BCDigest](instance: T): () => Digest = {
    val unwrapper = new BCDigestUnwrapper(instance)
    () => unwrapper.getInstance
  }

  private[this] val lookupTable = Map[String, () => Digest](
    // Blake
    "blake2b256" -> makeDigest(new digest.Blake2b.Blake2b256),
    "blake2b384" -> makeDigest(new digest.Blake2b.Blake2b384),
    "blake2b512" -> makeDigest(new digest.Blake2b.Blake2b512),
    "blake2s128" -> makeDigest(new digest.Blake2s.Blake2s128),
    "blake2s256" -> makeDigest(new digest.Blake2s.Blake2s256),
    "blake3256" -> makeDigest(new digest.Blake3.Blake3_256),
    // DSTU7564
    "dstu7564256" -> makeDigest(new digest.DSTU7564.Digest256),
    "dstu7564384" -> makeDigest(new digest.DSTU7564.Digest384),
    "dstu7564512" -> makeDigest(new digest.DSTU7564.Digest512),
    // GOST3411
    "gost3411256" -> makeDigest(new digest.GOST3411.Digest()),
    // Haraka
    "haraka256" -> makeDigest(new digest.Haraka.Digest256),
    "haraka512" -> makeDigest(new digest.Haraka.Digest512),
    // Keccak
    "keccak256" -> makeDigest(new digest.Keccak.Digest256),
    "keccak384" -> makeDigest(new digest.Keccak.Digest384),
    "keccak512" -> makeDigest(new digest.Keccak.Digest512),
    // SHA
    "sha256" -> makeDigest(new digest.SHA256.Digest),
    "sha384" -> makeDigest(new digest.SHA384.Digest),
    "sha512" -> makeDigest(new digest.SHA512.Digest),
    // SHA3
    "sha3256" -> makeDigest(new digest.SHA3.Digest256),
    "sha3384" -> makeDigest(new digest.SHA3.Digest384),
    "sha3512" -> makeDigest(new digest.SHA3.Digest512),
    // Skein-256
    "skein256128" -> makeDigest(new digest.Skein.Digest_256_128),
    "skein256256" -> makeDigest(new digest.Skein.Digest_256_256),
    // Skein-512
    "skein512128" -> makeDigest(new digest.Skein.Digest_512_128),
    "skein512256" -> makeDigest(new digest.Skein.Digest_512_256),
    "skein512384" -> makeDigest(new digest.Skein.Digest_512_256),
    "skein512512" -> makeDigest(new digest.Skein.Digest_512_256),
    // Tiger
    "tiger" -> makeDigest(new digest.Tiger.Digest),
    // Whirlpool
    "whirlpool" -> makeDigest(new digest.Whirlpool.Digest),
  )

  private final def getDigestByName(name: String): Digest =
    lookupTable.get(name.toLowerCase.stripPrefix("hmac").replaceAll("""-""", "")) match {
      case Some(fn) => fn()
      case None     => throw new ClassNotFoundException(s"No digest found with name: $name")
    }

}
