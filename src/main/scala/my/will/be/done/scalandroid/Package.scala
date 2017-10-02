package my.will.be.done.scalandroid

import scala.language.implicitConversions
import se.vidstige.jadb.managers.{Package ⇒ JPackage}

case class Package(name: String) {
  lazy val pkg: JPackage = new JPackage(name)
}

object Package {
  implicit def fromName(name: String): Package = {
    Package(name)
  }

  implicit def apply(jPackage: JPackage): Package = {
    new Package(jPackage.toString) {
      override lazy val pkg = jPackage
    }
  }
}
