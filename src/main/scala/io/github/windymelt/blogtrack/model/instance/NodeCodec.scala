package io.github.windymelt.blogtrack.model
package instance

import com.github.nscala_time.time.Imports.*
import neotypes.mappers.ResultMapper
import neotypes.generic.implicits._
trait NodeCodec {
  given ResultMapper[DateTime] = ResultMapper.string.map(DateTime.parse)

  given mapper: ResultMapper[Node] = ResultMapper.productDerive[Node]
}
object NodeCodec extends NodeCodec
