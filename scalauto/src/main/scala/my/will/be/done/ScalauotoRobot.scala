package my.will.be.done.scalauto

import java.awt.{
  GraphicsDevice,
  Robot,
  Rectangle,
  Toolkit,
  MouseInfo,
  Point,
  Dimension
}
import java.awt.image.BufferedImage
import java.awt.geom.Point2D
import java.awt.event.InputEvent
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.concurrent.duration.Duration

class ScalautoRobot(val robot: Robot) extends Scalauto {
  override type Feedback = Unit
  override implicit val executionContext = ExecutionContext.global

  override def mouseLocation: Future[Point2D] = {
    Future {
      val point = MouseInfo.getPointerInfo.getLocation
      new Point(point.x, point.y)
    }
  }
  override def mouseMove(to: Point2DMagnet): Future[Feedback] = {
    to.point2D.map { point ⇒
      robot.mouseMove(point.getX.toInt, point.getY.toInt)
    }
  }

  override def mouseDown(at: Option[Point2DMagnet]): Future[Feedbacks] = {
    for {
      moveFeedback ← at.fold(Future.unit)(mouseMove)
    } yield {
      Seq(
        moveFeedback,
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
      )
    }
  }

  override def mouseUp(at: Option[Point2DMagnet]): Future[Feedbacks] = {
    for {
      moveFeedback ← at.fold(Future.unit)(mouseMove)
    } yield {
      Seq(
        moveFeedback,
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
      )
    }
  }

  override def delay[O](before: Duration,
                        after: Duration,
                        operation: Operation[O]): Future[O] = {
    val promise = Promise[O]()
    robot.delay(before.toMillis.toInt)
    operation().map { o ⇒
      robot.delay(after.toMillis.toInt)
      promise.success(o)
    }
    promise.future
  }

  override def screenshot(
      rectangle: Option[Rectangle] = None): Future[BufferedImage] = {
    for {
      rect ← rectangle.fold(screenSize.map(new Rectangle(_)))(
        Future.successful)
    } yield {
      robot.createScreenCapture(rect)
    }
  }

  override def screenSize: Future[Dimension] = {
    Future {
      Toolkit.getDefaultToolkit.getScreenSize
    }
  }
}

object ScalautoRobot {
  def apply(): ScalautoRobot = {
    new ScalautoRobot(new Robot())
  }

  def apply(screen: GraphicsDevice): ScalautoRobot = {
    new ScalautoRobot(new Robot(screen))
  }
}
