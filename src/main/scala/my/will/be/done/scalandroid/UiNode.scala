package my.will.be.done.scalandroid

import scala.xml.{XML, Node}
import java.io.File
import java.awt.Rectangle

case class UiNode(
    index: Int,
    text: String,
    `resource-id`: String,
    `class`: String,
    `package`: Package,
    `content-desc`: String,
    checkable: Boolean,
    checked: Boolean,
    clickable: Boolean,
    enabled: Boolean,
    focusable: Boolean,
    focused: Boolean,
    scrollable: Boolean,
    `long-clickable`: Boolean,
    password: Boolean,
    selected: Boolean,
    bounds: Rectangle,
    parent: Option[UiNode],
    kids: Seq[UiNode]
) extends Traversable[UiNode] {
  def foreachKid[U](parent: UiNode, kids: Seq[UiNode], f: UiNode ⇒ U): Unit = {
    kids.foreach(f)
    kids.foreach { kid ⇒
      foreachKid(kid, kid.kids, f)
    }
  }

  override def foreach[U](f: UiNode ⇒ U): Unit = {
    f(this)
    foreachKid(this, kids, f)
  }

  def center: Point = bounds.getCenterX → bounds.getCenterY

  /**
    * @return this node with niether parents nor children (easier to browse/serialize)
    */
  def only: UiNode = copy(kids = Nil, parent = None)

  def ancestors: Iterator[UiNode] = {
    Iterator
      .iterate(Option(this))(_.flatMap(_.parent))
      .drop(1)
      .takeWhile(_.nonEmpty)
      .flatten
  }
}

object UiNode {
  val Tag = "node"
  val BoundsRegex = """\[(\d+),(\d+)\]\[(\d+),(\d+)\]""".r
  def apply(hierarchy: File): UiNode = {
    UiNode((XML.loadFile(hierarchy) \ Tag).head, None)
  }

  def apply(node: Node, parent: Option[UiNode]): UiNode = {
    val BoundsRegex(x1, y1, x2, y2) = node \@ "bounds"
    val kid = UiNode(
      `index` = (node \@ "index").toInt,
      `text` = (node \@ "text"),
      `resource-id` = (node \@ "resource-id"),
      `class` = (node \@ "class"),
      `package` = Package(node \@ "package"),
      `content-desc` = (node \@ "content-desc"),
      `checkable` = (node \@ "checkable").toBoolean,
      `checked` = (node \@ "checked").toBoolean,
      `clickable` = (node \@ "clickable").toBoolean,
      `enabled` = (node \@ "enabled").toBoolean,
      `focusable` = (node \@ "focusable").toBoolean,
      `focused` = (node \@ "focused").toBoolean,
      `scrollable` = (node \@ "scrollable").toBoolean,
      `long-clickable` = (node \@ "long-clickable").toBoolean,
      `password` = (node \@ "password").toBoolean,
      `selected` = (node \@ "selected").toBoolean,
      bounds = new Rectangle(x1.toInt,
                             y1.toInt,
                             x2.toInt - x1.toInt,
                             y2.toInt - y1.toInt),
      parent = parent,
      kids = Nil
    )
    kid.copy(
      kids = (node \ Tag).map(UiNode(_, Option(kid)))
    )
  }
}
