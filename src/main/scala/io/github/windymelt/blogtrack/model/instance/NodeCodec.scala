package io.github.windymelt.blogtrack
package model
package instance

import com.github.nscala_time.time.Imports.*
import neotypes.generic.implicits._
import neotypes.mappers.ResultMapper

/** Neo4jのライブラリであるNeoTypesに各種の型を渡すための変換グッズ。
  */
trait NodeCodec {

  /** [[org.joda.time.DateTime]] は文字列を通じて受け渡し可能であることを定義する。
    */
  given ResultMapper[DateTime] = ResultMapper.string.map(DateTime.parse)

  /** [[model.Node]]は自動的に導出可能であることを定義する。
    */
  given mapper: ResultMapper[Node] = ResultMapper.productDerive[Node]
}
object NodeCodec extends NodeCodec
