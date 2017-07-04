package my.will.be.done.scalauto

import scala.language.implicitConversions
import scala.concurrent.Future
import java.awt.geom.Point2D

trait Point2DMagnet {
  def point2D: Future[Point2D]
}

object Point2DMagnet {
  implicit def fromXY[X, Y](xY: (X, Y))(implicit numericX: Numeric[X],
                                        numericY: Numeric[Y]): Point2DMagnet =
    new Point2DMagnet {
      val (x, y) = xY
      def point2D: Future[Point2D] = {
        Future.successful(
          new Point2D.Double(numericX.toDouble(x), numericY.toDouble(y))
        )
      }
    }

  implicit def fromPoint2D(point: Point2D): Point2DMagnet = new Point2DMagnet {
    def point2D: Future[Point2D] = Future.successful(point)
  }
}
