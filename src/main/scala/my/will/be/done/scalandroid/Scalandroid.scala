package my.will.be.done.scalandroid

import se.vidstige.jadb.{Stream, JadbDevice, JadbConnection}
import se.vidstige.jadb.managers.PackageManager
import java.awt.Dimension
import scala.io.Source
import java.nio.file.Files
import java.io.{InputStream, File, FileOutputStream}
import scala.concurrent.duration.FiniteDuration
import scala.collection.JavaConverters._
import scala.io.Source

class Scalandroid(val jadbDevice: JadbDevice) {
  val packageManager = new PackageManager(jadbDevice)

  def input(args: String*): InputStream = {
    execute("input", args: _*)
  }

  def execute(command: String, args: String*): InputStream = {
    jadbDevice.execute(command, args: _*)
  }

  def push(local: Path.Local, remote: Path.Remote): Unit = {
    jadbDevice.push(local.file, remote.file)
  }

  def pull(remote: Path.Remote): File = {
    val local = Files.createTempFile(null, null).toFile
    local.deleteOnExit
    jadbDevice.pull(remote.file, local)
    local
  }

  val uiautomatorDumpRegex = "UI hierchary dumped to: (.+)".r
  def uiautomatorDump: UiNode = {
    // TODO: not strictly necessary, but needed to use up cycles here anyway or it would pull before it was done taking a dump
    val remote = Source
      .fromInputStream(execute("uiautomator", "dump", "--verbose"))
      .mkString
      .trim match {
      case uiautomatorDumpRegex(path) ⇒
        Path.Remote(path)
    }
    val local = pull(remote)
    execute("rm", remote.path)
    UiNode(local)
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
    Source.fromInputStream(execute("wm", "size")).mkString match {
      case screenSizeRegex(width, height) ⇒
        new Dimension(width.toInt, height.toInt)
    }
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

  def monkey(args: String*): InputStream = {
    execute("monkey", args: _*)
  }

  def packages: Seq[Pkg] =
    packageManager.getPackages.asScala.map(Pkg(_))

  def launch(pkg: Pkg): InputStream = {
    // packageManager.launch(pkg.pkg) // TODO: doesn't work for some reason
    monkey("-p", pkg.name, "-c", "android.intent.category.LAUNCHER", "1")
  }

  def startActivity(pkg: Pkg, activity: String): InputStream = {
    execute("am", "start", s"${pkg.name}/.$activity")
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
}

object Scalandroid {
  def apply(): Scalandroid = {
    new Scalandroid(jadbDevice = new JadbConnection().getAnyDevice)
  }
}
