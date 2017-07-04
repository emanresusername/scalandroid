package my.will.be.done.scalauto.android

import se.vidstige.jadb.{JadbConnection, JadbDevice}
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import java.awt.geom.Point2D
import java.io.InputStream
import my.will.be.done.scalauto.{Point2DOps, Point2DMagnet}

class ScalautoInput(val jadbDevice: JadbDevice) extends ScalautoAdb {
  override type Feedback = InputStream
  case object OnlyInputCommandSupported
      extends Exception(
        "this action is not supported by the `input` command. TODO: lower level `sendevent` implementation")

  val onlyInputCommandSupported = Future.failed(OnlyInputCommandSupported)

  override def mouseLocation: Future[Point2D] = onlyInputCommandSupported
  override def mouseMove(to: Point2DMagnet): Future[Feedback] =
    onlyInputCommandSupported
  override def mouseDown(at: Option[Point2DMagnet]): Future[Feedbacks] =
    onlyInputCommandSupported
  override def mouseUp(at: Option[Point2DMagnet]): Future[Feedbacks] =
    onlyInputCommandSupported

  def input(args: String*): Future[Feedback] = {
    executeShell("input", args: _*)
  }

  override def mouseClick(
      at: Option[Point2DMagnet] = None,
      duration: Duration = Duration.Zero): Future[Feedbacks] = {
    at → duration match {
      case (Some(point), Duration.Zero) ⇒
        tap(point)
      case _ ⇒
        onlyInputCommandSupported
    }
  }

  def tap(at: Point2DMagnet): Future[Feedbacks] = {
    for {
      point ← at.point2D
      x = point.getX.toString
      y = point.getY.toString
      feedback ← input("tap", x, y)
    } yield {
      Seq(feedback)
    }
  }

  def swipe(from: Point2DMagnet,
            to: Point2DMagnet,
            duration: Duration = Duration.Zero): Future[Feedbacks] = {
    for {
      point1 ← from.point2D
      point2 ← to.point2D
      x1 = point1.getX.toString
      y1 = point1.getY.toString
      x2 = point2.getX.toString
      y2 = point2.getY.toString
      feedback ← input("swipe", x1, y1, x2, y2, duration.toMillis.toString)
    } yield {
      Seq(feedback)
    }
  }
  def swipeLeft(from: Point2DMagnet,
                distance: Int,
                duration: Duration): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateLeft(distance)
      feedbacks ← swipe(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
  def swipeRight(from: Point2DMagnet,
                 distance: Int,
                 duration: Duration): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateRight(distance)
      feedbacks ← swipe(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
  def swipeUp(from: Point2DMagnet,
              distance: Int,
              duration: Duration): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateUp(distance)
      feedbacks ← swipe(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
  def swipeDown(from: Point2DMagnet,
                distance: Int,
                duration: Duration): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateDown(distance)
      feedbacks ← swipe(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
}

object ScalautoInput {
  def apply(): ScalautoInput = {
    new ScalautoInput(new JadbConnection().getAnyDevice)
  }
}
