package my.will.be.done.scalauto.scalandroid

import se.vidstige.jadb.{Stream, JadbDevice, JadbConnection}
import java.awt.Dimension
import scala.io.Source
import java.io.{InputStream, File, FileOutputStream}
import net.sourceforge.tess4j.Tesseract
import scala.concurrent.duration.{FiniteDuration, Duration}
import my.will.be.done.scalauto.{TemplateMatcher, TemplateMatch}
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

  def tap(x: Double, y: Double): InputStream = {
    input("tap", x.toString, y.toString)
  }

  def text(text: String): InputStream = {
    input("text", text)
  }

  def swipe(x1: Double,
            y1: Double,
            x2: Double,
            y2: Double,
            duration: FiniteDuration): InputStream = {
    input("swipe",
          x1.toString,
          y1.toString,
          x2.toString,
          y2.toString,
          duration.toMillis.toString)
  }
  def swipeLeft(x: Double,
                y: Double,
                distance: Int,
                duration: FiniteDuration): InputStream = {
    swipe(x1 = x, y1 = y, x2 = x - distance, y2 = y, duration = duration)
  }
  def swipeRight(x: Double,
                 y: Double,
                 distance: Int,
                 duration: FiniteDuration): InputStream = {
    swipe(x1 = x, y1 = y, x2 = x + distance, y2 = y, duration = duration)
  }
  def swipeUp(x: Double,
              y: Double,
              distance: Int,
              duration: FiniteDuration): InputStream = {
    swipe(x1 = x, y1 = y, x2 = x, y2 = y - distance, duration = duration)
  }
  def swipeDown(x: Double,
                y: Double,
                distance: Int,
                duration: FiniteDuration): InputStream = {
    swipe(x1 = x, y1 = y, x2 = x, y2 = y + distance, duration = duration)
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
        x = rectangle.getCenterX,
        y = rectangle.getCenterY
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
