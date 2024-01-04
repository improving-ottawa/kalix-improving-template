package com.improving.hashing

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Duration
import java.security.SecureRandom

class XXHashSpec extends AnyWordSpec with Matchers {

  val secureRng = new SecureRandom()

  private def timeHashBlock(block: Array[Byte]): (Duration, Long) = {
    val start = System.nanoTime()
    val hash  = XXHash.hashByteArray(block)
    val end   = System.nanoTime()

    val elapsed = Duration.ofNanos(end - start)
    (elapsed, hash)
  }

  "XXHash" should {

    "have a high hashing throughput" in {
      System.gc()

      // 64MB block of random data
      val blockSizeMB = 64
      val largeBlock  = Array.ofDim[Byte](blockSizeMB * 1024 * 1024)
      secureRng.nextBytes(largeBlock)

      // Warm-up
      XXHash.hashByteArray(largeBlock)

      // Collect 100 samples
      val samples = (1 to 100).map(_ => timeHashBlock(largeBlock))

      // Check all hashes are consistent
      val firstHash = samples.head._2
      samples.map { case (_, hash) => hash mustBe firstHash }

      // Calculate the throughput
      def secondsNano(dur: java.time.Duration): Double =
        (BigDecimal(dur.getNano) / BigDecimal(1000000000.0)).toDouble

      val thrptSamples = samples.map { case (dur, _) => blockSizeMB / secondsNano(dur) }

      // Calculate percentiles/avg
      val avgThrpt = thrptSamples.sum / 100.0
      val ordered  = thrptSamples.sorted

      info(f"XXHash (5th):  ${ordered(4)}%,.03f MB/sec")
      info(f"XXHash (Med):  ${ordered(49)}%,.03f MB/sec")
      info(f"XXHash (95th): ${ordered(94)}%,.03f MB/sec")
      info(f"XXHash (Avg):  $avgThrpt%,.03f MB/sec")

      System.gc()
      succeed
    }

    "be very sensitive to input data" in {
      System.gc()

      // 64MB block of random data
      val blockSizeMB = 64
      val largeBlock  = Array.ofDim[Byte](blockSizeMB * 1024 * 1024)
      secureRng.nextBytes(largeBlock)

      val h1 = XXHash.hashByteArray(largeBlock)

      // flip 8 bits in 64MB block
      val b1    = largeBlock(1024 * 1024)
      // Ignore IntelliJ telling you that you don't need the `toByte` here, its stupid.
      val b1Inv = (~b1).toByte

      largeBlock(1024 * 1024) = b1Inv
      val h2 = XXHash.hashByteArray(largeBlock)

      System.gc()
      h1 must not be h2
    }

  }

}
