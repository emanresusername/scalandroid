scalaVersion in ThisBuild := "2.12.3"
scalafmtOnCompile in ThisBuild := true

lazy val commonSettings = Seq(
  organization := "my.will.be.done",
  resolvers += "jitpack" at "https://jitpack.io",
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint")
)

lazy val scalauto = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= {
      Seq(
        "org.boofcv" % "core" % "0.26"
      )
    }
  )

lazy val `scalauto-android` = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= {
      Seq(
        // need raw `execute` for screencap -p to work
        "com.github.vidstige" % "jadb" % "a65c81343f7548992c91dcca7362131c2d27e824"
      )
    }
  )
  .dependsOn(scalauto)
