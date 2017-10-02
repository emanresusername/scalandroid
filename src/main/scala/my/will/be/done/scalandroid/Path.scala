package my.will.be.done.scalandroid

import se.vidstige.jadb.RemoteFile
import java.io.File

sealed abstract class Path(path: String)

object Path {
  case class Remote(path: String) extends Path(path) {
    def file: RemoteFile = new RemoteFile(path)
  }
  case class Local(path: String) extends Path(path) {
    def file: File = new File(path)
  }
}
