package my.will.be.done.scalauto

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import java.awt.geom.Point2D
import my.will.be.done.scalauto.tween.{Tween, StepPerPixelLinear}

trait MouseAutomator extends Delayer {
  type Feedback
  type Feedbacks = Seq[Feedback]
  type Path      = Seq[(Double, Double)]

  def mouseLocation: Future[Point2D]
  case object MouseLocation extends Point2DMagnet {
    def point2D: Future[Point2D] = mouseLocation
  }
  def mouseMove(to: Point2DMagnet): Future[Feedback]
  def mouseDown(at: Option[Point2DMagnet]): Future[Feedbacks]
  def mouseUp(at: Option[Point2DMagnet]): Future[Feedbacks]

  def mouseMove[Delta](
      to: Point2DMagnet,
      duration: Duration = Duration.Zero,
      from: Point2DMagnet = MouseLocation,
      tween: Tween[Point2D, Delta] = StepPerPixelLinear): Future[Feedbacks] = {
    for {
      from ← from.point2D
      to   ← to.point2D
      path         = tween.path(from, to).toSeq
      steps        = path.length
      stepDuration = duration / steps
      feedbacks ← delayBetween(stepDuration, path.map { point ⇒ () ⇒
        mouseMove(point)
      })
    } yield {
      feedbacks
    }
  }
  def mouseMoveLeft(distance: Int,
                    duration: Duration = Duration.Zero,
                    from: Point2DMagnet = MouseLocation): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateLeft(distance)
      feedbacks ← mouseMove(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
  def mouseMoveRight(
      distance: Int,
      duration: Duration = Duration.Zero,
      from: Point2DMagnet = MouseLocation): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateRight(distance)
      feedbacks ← mouseMove(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
  def mouseMoveUp(distance: Int,
                  duration: Duration = Duration.Zero,
                  from: Point2DMagnet = MouseLocation): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateUp(distance)
      feedbacks ← mouseMove(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
  def mouseMoveDown(distance: Int,
                    duration: Duration = Duration.Zero,
                    from: Point2DMagnet = MouseLocation): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateDown(distance)
      feedbacks ← mouseMove(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }

  def mouseClick(at: Option[Point2DMagnet] = None,
                 duration: Duration = Duration.Zero): Future[Feedbacks] = {
    for {
      mouseDownFeedbacks ← mouseDown(at)
      mouseUpFeedbacks   ← delayBefore(duration, () ⇒ mouseUp(at))
    } yield {
      mouseDownFeedbacks ++ mouseUpFeedbacks
    }
  }

  def mouseDrag[Delta](
      to: Point2DMagnet,
      duration: Duration,
      from: Point2DMagnet = MouseLocation,
      tween: Tween[Point2D, Delta] = StepPerPixelLinear): Future[Feedbacks] = {
    for {
      mouseDownFeedbacks ← mouseDown(Option(from))
      mouseMoveFeedbacks ← mouseMove(from = from,
                                     to = to,
                                     duration = duration,
                                     tween = tween)
      mouseUpFeedbacks ← mouseUp(Option(to))
    } yield {
      mouseDownFeedbacks ++ mouseMoveFeedbacks ++ mouseUpFeedbacks
    }
  }
  def mouseDragLeft(distance: Int,
                    duration: Duration,
                    from: Point2DMagnet = MouseLocation): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateLeft(distance)
      feedbacks ← mouseDrag(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
  def mouseDragRight(
      distance: Int,
      duration: Duration,
      from: Point2DMagnet = MouseLocation): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateRight(distance)
      feedbacks ← mouseDrag(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
  def mouseDragUp(distance: Int,
                  duration: Duration,
                  from: Point2DMagnet = MouseLocation): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateUp(distance)
      feedbacks ← mouseDrag(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
  def mouseDragDown(distance: Int,
                    duration: Duration,
                    from: Point2DMagnet = MouseLocation): Future[Feedbacks] = {
    for {
      start ← from.point2D
      end = start.translateDown(distance)
      feedbacks ← mouseDrag(from = start, to = end, duration = duration)
    } yield {
      feedbacks
    }
  }
}
