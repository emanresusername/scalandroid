package my.will.be.done.scalauto.tween

import java.awt.geom.Point2D
import my.will.be.done.scalauto.Point2DOps

trait Tween[Point, Delta] {
  def delta(from: Point, to: Point): Delta
  def nextPoint(point: Point, delta: Delta): Point
  def onPath(point: Point, from: Point, to: Point): Boolean

  def path(from: Point, to: Point): Iterator[Point] = {
    val delta = this.delta(from, to)
    Iterator
      .iterate(from)(nextPoint(_, delta))
      .takeWhile(onPath(_, from, to))
  }
}

trait Linear extends Tween[Point2D, (Double, Double)] {
  def steps(from: Point2D, to: Point2D): Int

  override def delta(from: Point2D, to: Point2D): (Double, Double) = {
    val steps = this.steps(from, to).toDouble
    val dx    = (to.getX - from.getX) / steps
    val dy    = (to.getY - from.getY) / steps
    dx → dy
  }
  override def nextPoint(point: Point2D, delta: (Double, Double)): Point2D = {
    point.translate(dx = delta._1, dy = delta._2)
  }
}

case object StepPerPixelLinear extends Linear {
  override def steps(from: Point2D, to: Point2D): Int = {
    from.distance(to).ceil.toInt
  }
  override def onPath(point: Point2D, from: Point2D, to: Point2D): Boolean = {
    from.lineTo(to).ptSegDist(point) < 1d
  }
}
