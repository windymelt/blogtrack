package io.github.windymelt.blogtrack
package widget

import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import cats.effect.unsafe.implicits.global

import org.scalajs.dom

// import javascriptLogo from "/javascript.svg"
@js.native @JSImport("/javascript.svg", JSImport.Default)
val javascriptLogo: String = js.native

val citationApiUrl = "http://localhost:8080/citations"
val myBlogRegex = """https://blog\.3qe\.us/.*|http://localhost.*|http://127\.0\.0\.1.*""".r

@main
def Main(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Widget.appElement,
  )

object Widget {
  case class Citation(
      title: String,
      url: String,
      description: String,
      tags: Seq[String] = Seq(),
  )

  val citations = Seq(
    Citation(
      "JavaScript",
      "https://developer.mozilla.org/en-US/docs/Web/JavaScript",
      "JavaScript (JS) is a lightweight, interpreted, or just-in-time compiled programming language with first-class functions. While it is most well-known as the scripting language for Web pages, many non-browser environments also use it, such as Node.js, Apache CouchDB and Adobe Acrobat. JavaScript is a prototype-based, multi-paradigm, single-threaded, dynamic language, supporting object-oriented, imperative, and declarative (e.g. functional programming) styles.",
    ),
    Citation(
      "Scala",
      "https://www.scala-lang.org/",
      "Scala combines object-oriented and functional programming in one concise, high-level language. Scala's static types help avoid bugs in complex applications, and its JVM and JavaScript runtimes let you build high-performance systems with easy access to huge ecosystems of libraries.",
      tags = Seq("scala"),
    ),
    Citation(
      "Scala.js",
      "https://www.scala-js.org/",
      "Scala.js compiles Scala code to JavaScript, allowing you to write your Web application entirely in Scala! It aims at seamless integration with JavaScript libraries and an easy development cycle, in order to get you productive as fast as possible.",
        tags = Seq("scala", "javascript"),
    ),
  )

  val citationVar: Var[Seq[Citation]] = Var(citations)

  def getCitations(): Future[Seq[Citation]] = { // stub
    val currentUrl = dom.window.location.href
    currentUrl match {
      case myBlogRegex() =>
        val io = BlogTrackClient.blogTrackClient.use { c =>
          c.readCite(api.Url(currentUrl))
        }
        io.unsafeToFuture()
        Future.successful(citations)
      case _             => Future.failed(new Exception("Not my blog"))
    }
  }
  def appElement: Element = div(
    cls := "ui card container",
    div(
      cls := "content",
      div(
        div(
          cls := "header",
          "This article is cited by:",
        ),
        cls := "ui relaxed divided list",
        children <-- citationVar.signal.map(
          _.map(citationElement),
        ),
        Signal.fromFuture(getCitations()) --> { citations =>
          citations match {
            case Some(citations) => citationVar.set(citations)
            case None            => // TODO: error handling
          }
        }
      ),
    ),
  )

  def citationElement(
      citation: Citation
  ): Element = {
    div(
      cls := "item",
      a(
        href := citation.url,
        target := "_blank",
        div(
          cls := "content",
          div(
            cls := "header",
            i(
              cls := "linkify icon",
            ),
            a(citation.title),
            citation.tags.map { tag =>
              span(
                cls := "ui label",
                tag,
              )
            },
          ),
          div(
            cls := "description",
            citation.description,
          ),
        ),
      )

    )
  }
}
