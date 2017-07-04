package my.will.be.done.scalauto.android

import se.vidstige.jadb.{Stream, JadbDevice}
import scala.concurrent.{Future, ExecutionContext}
import java.awt.{Dimension, Rectangle}
import java.awt.image.BufferedImage
import my.will.be.done.scalauto.{
  Scalauto,
  BufferedImageMagnet,
  SingleThreadScheduledExecutorDelayer
}
import java.io.{InputStream, File, FileOutputStream}
import scala.io.Source

trait ScalautoAdb extends Scalauto with SingleThreadScheduledExecutorDelayer {
  val jadbDevice: JadbDevice

  override implicit val executionContext =
    ExecutionContext.fromExecutor(scheduler)

  def executeShell(command: String, args: String*): Future[InputStream] = {
    Future {
      jadbDevice.executeShell(command, args: _*)
    }
  }

  def execute(command: String, args: String*): Future[InputStream] = {
    Future {
      jadbDevice.execute(command, args: _*)
    }
  }

  // TODO: ImageIO can't read from raw inputstreams
  object ScreencapHelper {
    val TmpFile = File.createTempFile("screencap", ".png")
    TmpFile.deleteOnExit()

    def apply(pngStream: InputStream): BufferedImageMagnet = {
      new BufferedImageMagnet {
        def bufferedImage: Future[BufferedImage] = {
          Future[BufferedImageMagnet] {
            val outputStream = new FileOutputStream(TmpFile)
            Stream.copy(pngStream, outputStream)
            TmpFile
          }.flatMap(_.bufferedImage)
        }
      }
    }
  }

  override def screenshot(
      rectangle: Option[Rectangle] = None): Future[BufferedImage] = {
    for {
      inputStream   ← execute("screencap", "-p")
      bufferedImage ← ScreencapHelper(inputStream).bufferedImage
    } yield {
      bufferedImage
    }
  }

  val screenSizeRegex = """Override size: (\d+)x(\d+)""".r.unanchored

  override def screenSize: Future[Dimension] = {
    for {
      stdout ← executeShell("wm", "size")
      screenSizeRegex(width, height) = Source.fromInputStream(stdout).mkString
    } yield {
      new Dimension(width.toInt, height.toInt)
    }
  }
}
