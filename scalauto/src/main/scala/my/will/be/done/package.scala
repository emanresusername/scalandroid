package my.will.be.done

import java.awt.geom.{Point2D, AffineTransform, Line2D},
AffineTransform.getTranslateInstance
import scala.collection.JavaConverters._
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File

package object scalauto {
  implicit class BufferedImageOps(bufferedImage: BufferedImage) {
    def formatGuess: Option[String] = {
      ImageIO
        .getImageReaders(
          ImageIO.createImageInputStream(bufferedImage)
        )
        .asScala
        .map(_.getFormatName)
        .find(_ != null)
    }

    def write(file: File, formatName: Option[String] = formatGuess): Boolean = {
      formatName match {
        case None ⇒
          throw new Exception("did not supply, and cannot guess format name")
        case Some(format) ⇒
          ImageIO.write(bufferedImage, format, file)
      }
    }
  }

  implicit class Point2DOps(point: Point2D) {
    def translate(dx: Double = 0, dy: Double = 0): Point2D = {
      getTranslateInstance(dx, dy).transform(point, null)
    }
    def translateLeft(delta: Double): Point2D = {
      translate(dx = -delta)
    }
    def translateRight(delta: Double): Point2D = {
      translate(dx = delta)
    }
    def translateUp(delta: Double): Point2D = {
      translate(dy = -delta)
    }
    def translateDown(delta: Double): Point2D = {
      translate(dy = delta)
    }
    def lineTo(other: Point2D): Line2D = {
      new Line2D.Double(point, other)
    }
  }
}
