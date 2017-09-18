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
import java.awt.Rectangle
import javax.imageio.ImageIO
import java.io.File

case class TemplateMatch(rectangle: Rectangle, score: Double)

/**
  * https://github.com/lessthanoptimal/BoofCV/blob/master/examples/src/boofcv/examples/features/ExampleTemplateMatching.java
  */
trait TemplateMatcher {

  def templateMatches(image: BufferedImage,
                      template: BufferedImage,
                      maxMatches: Int): Seq[TemplateMatch] = {
    val templateGray = convertToGrayF32(template)
    for {
      `match` ← templateMatches(image = convertToGrayF32(image),
                                template = templateGray,
                                maxMatches = maxMatches)
    } yield {
      TemplateMatch(
        new Rectangle(
          `match`.x,
          `match`.y,
          templateGray.width,
          templateGray.height
        ),
        `match`.score
      )
    }
  }

  def templateMatches(image: File,
                      template: File,
                      maxMatches: Int): Seq[TemplateMatch] = {
    templateMatches(image = ImageIO.read(image),
                    template = ImageIO.read(template),
                    maxMatches = maxMatches)
  }

  /**
    * Demonstrates how to search for matches of a template inside an image
    *
    * @param image           Image being searched
    * @param template        Template being looked for
    * @param maxMatches      Maximum number of matches it will return
    * @return Seq of match location and scores
    */
  def templateMatches(image: GrayF32,
                      template: GrayF32,
                      maxMatches: Int): Seq[Match] = {
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

  def convertToGrayF32(image: BufferedImage): GrayF32 = {
    ConvertBufferedImage.convertFromSingle(image,
                                           null.asInstanceOf[GrayF32],
                                           classOf[GrayF32])
  }
}

object TemplateMatcher extends TemplateMatcher
