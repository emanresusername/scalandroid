scalaVersion in ThisBuild := "2.12.3"
scalafmtOnCompile in ThisBuild := true

organization := "my.will.be.done"
resolvers += "jitpack" at "https://jitpack.io"
scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint")

libraryDependencies ++= {
  Seq(
    // need raw `execute` for screencap -p to work
    "com.github.vidstige" % "jadb" % "4332cd6ab836736b69fbb8ebbeb6cd1becd4a422",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
  )
}
