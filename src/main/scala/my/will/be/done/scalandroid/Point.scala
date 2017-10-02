package my.will.be.done.scalandroid

import scala.language.implicitConversions

case class Point(x: Double, y: Double) {
  def right(dx: Double): Point = copy(x = x + dx)
  def left(dx: Double): Point = copy(x = x - dx)
  def down(dy: Double): Point = copy(y = y + dy)
  def up(dy: Double): Point = copy(y = y - dy)
}

object Point {
  implicit def fromXY[X, Y](xY: (X, Y))(implicit numericX: Numeric[X],
                                        numericY: Numeric[Y]): Point = {
    Point(numericX.toDouble(xY._1), numericY.toDouble(xY._2))
  }
}
