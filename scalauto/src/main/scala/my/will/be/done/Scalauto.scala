package my.will.be.done.scalauto

import java.awt.{Dimension, Rectangle}
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import java.awt.image.BufferedImage

trait Scalauto extends MouseAutomator with ImageFinder {
  def screenshot(rectangle: Option[Rectangle] = None): Future[BufferedImage]
  def screenSize: Future[Dimension] = {
    for {
      image ← screenshot(None)
    } yield {
      new Dimension(image.getWidth, image.getHeight)
    }
  }

  def findMatches(imageMagnet: BufferedImageMagnet,
                  maxMatches: Int): Future[Seq[ImageMatch]] = {
    for {
      fullImage  ← screenshot(None)
      rectangles ← findMatches(fullImage, imageMagnet, maxMatches)
    } yield {
      rectangles
    }
  }

  def clickMultiple(
      imageMagnet: BufferedImageMagnet,
      clickInterval: Duration,
      maxClicks: Int,
      score: Double,
      clickDuration: Duration = Duration.Zero): Future[Feedbacks] = {
    for {
      matches ← findMatches(imageMagnet, maxClicks)
      operations = for {
        imageMatch ← matches
        if imageMatch.score >= score
        rectangle = imageMatch.rectangle
        center    = rectangle.getCenterX → rectangle.getCenterY
      } yield { () ⇒
        mouseClick(at = Option(center), duration = clickDuration)
      }
      feedbacks ← delayBetween(clickInterval, operations)
    } yield {
      feedbacks.flatten
    }
  }

  def clickOne(imageMagnet: BufferedImageMagnet): Future[Feedbacks] = {
    clickMultiple(imageMagnet = imageMagnet,
                  clickDuration = Duration.Zero,
                  clickInterval = Duration.Zero,
                  maxClicks = 1,
                  score = 0)
  }

  def isOnScreen(imageMagnet: BufferedImageMagnet,
                 score: Double = 0): Future[Boolean] = {
    for {
      matches ← findMatches(imageMagnet, 1)
    } yield {
      matches.count(_.score >= score) > 0
    }
  }
}
