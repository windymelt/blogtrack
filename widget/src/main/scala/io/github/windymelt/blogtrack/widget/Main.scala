package io.github.windymelt.blogtrack
package widget

import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import cats.effect.unsafe.implicits.global
import org.scalajs.dom
import FomanticUI as F

val citationApiUrl = "http://localhost:8080/citations"
val bearerToken = ""
val myBlogRegex =
  """https://blog\.3qe\.us/.*|http://localhost.*|http://127\.0\.0\.1.*""".r

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
  
  val citationVar: Var[Option[Seq[Citation]]] = Var(None)

  def getCitations(): Future[Seq[Citation]] = { // stub
    // val currentUrl = dom.window.location.href
    val currentUrl =
      "https://blog.3qe.us/entry/2022/10/10/120114" // TODO: use href by prod or not
    currentUrl match {
      case myBlogRegex() =>
        val io =
          BlogTrackClient.blogTrackClient(citationApiUrl, bearerToken).use {
            c =>
              c.readCite(api.Url(currentUrl))
          }
        io.unsafeToFuture()
          .map(
            _.citation.whatCitedMe.map(cit =>
              Citation(cit.title, cit.url.toString, "", cit.tags)
            )
          )
      case _ => Future.failed(new Exception("Not my blog"))
    }
  }
  def appElement: Element = div(
    cls := s"${F.ui} ${F.card} ${F.container}",
    div(
      cls := F.content,
      div(
        cls <-- citationVar.signal.map {
          case Some(_) => s"${F.ui} ${F.inverted} ${F.dimmer}"
          case None    => s"${F.ui} ${F.active} ${F.inverted} ${F.dimmer}"
        },
        div(
          cls := s"${F.ui} ${F.text} ${F.loader}",
          "Loading",
        ),
      ),
      div(
        div(
          cls := F.header,
          "This article is cited by:",
        ),
        cls := s"${F.ui} ${F.relaxed} ${F.divided} ${F.list}",
        children <-- citationVar.signal.map { opt =>
          opt match {
            case Some(cit) => cit.map(citationElement)
            case None      => Seq()
          }
        },
        Signal.fromFuture(getCitations()) --> { citations =>
          citations match {
            case Some(citations) => citationVar.set(Some(citations))
            case None            => // TODO: error handling
          }
        },
      ),
    ),
  )

  def citationElement(
      citation: Citation
  ): Element = {
    div(
      cls := F.item,
      a(
        href := citation.url,
        target := "_blank",
        div(
          cls := F.content,
          div(
            cls := F.header,
            i(
              cls := s"${F.linkify} ${F.icon}"
            ),
            a(citation.title),
            citation.tags.map { tag =>
              span(
                cls := s"${F.ui} ${F.label}",
                tag,
              )
            },
          ),
          div(
            cls := F.description,
            citation.description,
          ),
        ),
      ),
    )
  }
}
