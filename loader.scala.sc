//> using scala 3.3.0
//> using dep io.bullet::borer-core::1.11.0
//> using dep io.bullet::borer-derivation::1.11.0
//> using dep "com.github.nscala-time::nscala-time::2.32.0"

import java.io.File
import com.github.nscala_time.time.Imports._
import io.bullet.borer.{Cbor, Encoder, Decoder}
import io.bullet.borer.derivation.ArrayBasedCodecs._

import io.bullet.borer.Cbor

case class Entry(
    url: String,
    title: String,
    tags: Seq[String],
    updatedAt: DateTime,
    cites: Set[String]
)

given Decoder[DateTime] =
  Decoder.forString.map[DateTime](DateTime.parse)

given Decoder[Entry] = deriveDecoder

val got = Cbor.decode(File(args(0))).to[Iterator[Entry]].value
println(got.take(10).toList)
