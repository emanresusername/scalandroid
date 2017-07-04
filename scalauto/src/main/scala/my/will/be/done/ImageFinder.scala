package my.will.be.done.scalauto

import java.awt.image.BufferedImage

import boofcv.factory.feature.detect.template.{
  FactoryTemplateMatching,
  TemplateScoreType
}
import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.feature.Match
import boofcv.struct.image.GrayF32

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import java.awt.Rectangle

case class ImageMatch(rectangle: Rectangle, score: Double)

trait ImageFinder {
  implicit val executionContext: ExecutionContext

  /**
    * Demonstrates how to search for matches of a template inside an image
    *
    * @param image           Image being searched
    * @param template        Template being looked for
    * @param maxMatches      Maximum number of matches it will return
    * @return Seq of match location and scores
    */
  def findMatches(image: GrayF32,
                  template: GrayF32,
                  maxMatches: Int): Future[Seq[Match]] = {
    Future {
      // create template matcher.
      val matcher =
        FactoryTemplateMatching.createMatcher(TemplateScoreType.SUM_DIFF_SQ,
                                              classOf[GrayF32])
      // Find the points which match the template the best
      matcher.setImage(image)
      matcher.setTemplate(template, null, maxMatches)
      matcher.process
      matcher.getResults.toList.asScala
    }
  }

  def findMatches(fullMagnet: BufferedImageMagnet,
                  partMagnet: BufferedImageMagnet,
                  maxMatches: Int): Future[Seq[ImageMatch]] = {
    for {
      fullImage     ← fullMagnet.bufferedImage
      partImage     ← partMagnet.bufferedImage
      fullConverted ← convertToGrayF32(fullImage)
      partConverted ← convertToGrayF32(partImage)
      matches       ← findMatches(fullConverted, partConverted, maxMatches)
    } yield {
      for {
        mach ← matches
      } yield {
        ImageMatch(
          new Rectangle(
            mach.x,
            mach.y,
            partConverted.width,
            partConverted.height
          ),
          mach.score
        )
      }
    }
  }

  def convertToGrayF32(image: BufferedImage): Future[GrayF32] = {
    Future {
      ConvertBufferedImage.convertFromSingle(image,
                                             null.asInstanceOf[GrayF32],
                                             classOf[GrayF32])
    }
  }
}
