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

  // TODO: less hacky: https://github.com/vidstige/jadb/issues/67
  def unicodeText(text: String) = {
    new ProcessBuilder(
      "adb",
      "shell",
      "am",
      "broadcast",
      "-a",
      "ADB_INPUT_TEXT",
      "--es",
      "msg",
      "'" ++ text.replaceAllLiterally("'", "\\'") ++ "'"
    ).start
  }
}

object Unicoid {
  def apply(): Unicoid = {
    new Scalandroid(jadbDevice = new JadbConnection().getAnyDevice)
    with Unicoid
  }
}
