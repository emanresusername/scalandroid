package my.will.be.done.scalauto

import java.awt.image.BufferedImage
import scala.language.implicitConversions
import scala.concurrent.{ExecutionContext, Future}
import javax.imageio.ImageIO
import java.io.File
import java.nio.file.Path

trait BufferedImageMagnet {
  def bufferedImage: Future[BufferedImage]
}

object BufferedImageMagnet {
  implicit def fromFile(source: File)(
      implicit executionContext: ExecutionContext): BufferedImageMagnet =
    new BufferedImageMagnet {
      def bufferedImage: Future[BufferedImage] = {
        Future {
          ImageIO.read(source)
        }
      }
    }

  implicit def fromFilePathString(path: String)(
      implicit executionContext: ExecutionContext): BufferedImageMagnet =
    new File(path)

  implicit def fromPath(path: Path)(
      implicit executionContext: ExecutionContext): BufferedImageMagnet =
    path.toFile

  implicit def fromBufferedImage(source: BufferedImage): BufferedImageMagnet =
    new BufferedImageMagnet {
      def bufferedImage: Future[BufferedImage] = {
        Future.successful(source)
      }
    }
}
