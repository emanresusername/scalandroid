package my.will.be.done.scalandroid

import scala.language.implicitConversions
import se.vidstige.jadb.managers.Package

case class Pkg(name: String) {
  lazy val `package`: Package = new Package(name)
}

object Pkg {
  implicit def fromName(name: String): Pkg = {
    Pkg(name)
  }

  implicit def apply(`pacakge`: Package): Pkg = {
    Pkg(`package`.toString)
  }
}
