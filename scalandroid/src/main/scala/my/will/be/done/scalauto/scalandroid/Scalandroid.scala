package my.will.be.done.scalauto.scalandroid

import se.vidstige.jadb.{Stream, JadbDevice, JadbConnection}
import java.awt.Dimension
import scala.io.Source
import java.io.{InputStream, File, FileOutputStream}
import net.sourceforge.tess4j.Tesseract
import scala.concurrent.duration.{FiniteDuration, Duration}
import my.will.be.done.scalauto.{TemplateMatcher, TemplateMatch, Point}
import monix.reactive.Observable

class Scalandroid(jadbDevice: JadbDevice, tessdataPrefix: String) {
  val tesseract = new Tesseract()
  tesseract.setDatapath(tessdataPrefix)

  def executeShell(command: String, args: String*): InputStream = {
    jadbDevice.executeShell(command, args: _*)
  }

  def input(args: String*): InputStream = {
    executeShell("input", args: _*)
  }

  def execute(command: String, args: String*): InputStream = {
    jadbDevice.execute(command, args: _*)
  }

  def screenshot: File = {
    val tmpFile = File.createTempFile("screencap", ".png")
    tmpFile.deleteOnExit()
    val outputStream = new FileOutputStream(tmpFile)
    Stream.copy(execute("screencap", "-p"), outputStream)
    tmpFile
  }

  val screenSizeRegex = """Override size: (\d+)x(\d+)""".r.unanchored

  def screenSize: Dimension = {
    Source.fromInputStream(executeShell("wm", "size")).mkString match {
      case screenSizeRegex(width, height) ⇒
        new Dimension(width.toInt, height.toInt)
    }
  }

  def ocr: String = {
    tesseract.doOCR(screenshot)
  }

  def tap(point: Point): InputStream = {
    input("tap", point.x.toString, point.y.toString)
  }

  def text(text: String): InputStream = {
    input("text", text)
  }

  def keycode(keycode: Keycode): InputStream = {
    input("keyevent", keycode.code.toString)
  }

  def swipe(from: Point, to: Point, duration: FiniteDuration): InputStream = {
    input("swipe",
          from.x.toString,
          from.y.toString,
          to.x.toString,
          to.y.toString,
          duration.toMillis.toString)
  }
  def swipeLeft(from: Point,
                distance: Int,
                duration: FiniteDuration): InputStream = {
    swipe(from = from, to = from left distance, duration = duration)
  }

  def swipeRight(from: Point,
                 distance: Int,
                 duration: FiniteDuration): InputStream = {
    swipe(from = from, to = from right distance, duration = duration)
  }

  def swipeUp(from: Point,
              distance: Int,
              duration: FiniteDuration): InputStream = {
    swipe(from = from, to = from up distance, duration = duration)
  }

  def swipeDown(from: Point,
                distance: Int,
                duration: FiniteDuration): InputStream = {
    swipe(from = from, to = from down distance, duration = duration)
  }

  def templateMatches(template: File, maxMatches: Int): Seq[TemplateMatch] = {
    TemplateMatcher.templateMatches(
      image = screenshot,
      template = template,
      maxMatches = maxMatches
    )
  }

  def tapMultiple(template: File,
                  tapInterval: FiniteDuration,
                  maxTaps: Int,
                  score: Double): Observable[InputStream] = {
    for {
      templateMatch ← Observable
        .fromIterable(
          templateMatches(template = template, maxMatches = maxTaps))
        .filter(_.score >= score)
        .delayOnNext(tapInterval)
      rectangle = templateMatch.rectangle
    } yield {
      tap(
        Point(
          rectangle.getCenterX,
          rectangle.getCenterY
        )
      )
    }
  }

  def tapOne(template: File): Observable[InputStream] = {
    tapMultiple(template = template,
                tapInterval = Duration.Zero,
                maxTaps = 1,
                score = 0)
  }

  def isOnScreen(template: File, score: Double = 0): Boolean = {
    templateMatches(template, 1).exists(_.score >= score)
  }
}

object Scalandroid {
  def apply(): Scalandroid = {
    new Scalandroid(
      jadbDevice = new JadbConnection().getAnyDevice,
      tessdataPrefix = "/home/linuxbrew/.linuxbrew/share/tessdata"
    )
  }
}
