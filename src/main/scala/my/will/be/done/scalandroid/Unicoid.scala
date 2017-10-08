package my.will.be.done.scalandroid

import se.vidstige.jadb.JadbConnection
import java.io.InputStream
import scala.io.Source

/**
  * Scalandroid extensions for inputing unicode text with https://github.com/senzhk/ADBKeyBoard
  */
trait Unicoid extends Scalandroid {
  def useAdbKeyboard: InputStream = {
    setInputMethod("com.android.adbkeyboard/.AdbIME")
  }

  def inputUnicode(unicode: String) =
    activityBroadcast("ADB_INPUT_TEXT", "--es", "msg", unicode)
  // TODO: things kept breaking with some ascii characters, simple \escapes didn't fix
  def inputAscii(ascii: String) = this.text(ascii)
  val asciiFilter: Char ⇒ Boolean = _ < 128
  val nonAsciiFilter: Char ⇒ Boolean = _ > 128

  def unicodeText(text: String): List[(String, String)] = {
    for {
      (span, notAscii, _) ← Iterator
        .iterate(("", asciiFilter(text.head), text)) {
          case (lastSpan, isAscii, rest) =>
            val filter = if (isAscii) asciiFilter else nonAsciiFilter
            val (span, nextRest) = rest.span(filter)
            (span, !isAscii, nextRest)
        }
        .drop(1)
        .takeWhile(_._1.nonEmpty)
        .toList
    } yield {
      (span → Source // read to make sure one finishes before next starts
        .fromInputStream(if (notAscii) {
          inputUnicode(span)
        } else {
          inputAscii("'" ++ span.replaceAllLiterally("'", "\\'") ++ "'")
        })
        .mkString)
    }
  }
}

object Unicoid {
  def apply(): Unicoid = {
    new Scalandroid(jadbDevice = new JadbConnection().getAnyDevice)
    with Unicoid
  }
}
