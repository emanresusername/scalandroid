scalaVersion in ThisBuild := "2.12.3"
scalafmtOnCompile in ThisBuild := true

lazy val commonSettings = Seq(
  organization := "my.will.be.done",
  resolvers += "jitpack" at "https://jitpack.io",
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint"),
  version := "0.0.1"
)

lazy val scalauto = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= {
      Seq(
        "org.boofcv" % "boofcv-core" % "0.27"
      )
    }
  )

lazy val scalandroid = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= {
      Seq(
        // need raw `execute` for screencap -p to work
        "com.github.vidstige"    % "jadb"   % "4332cd6ab836736b69fbb8ebbeb6cd1becd4a422",
        "io.monix"               %% "monix" % "2.3.0",
        "net.sourceforge.tess4j" % "tess4j" % "3.4.0"
      )
    }
  )
  .dependsOn(scalauto)

lazy val root = project
  .in(file("."))
  .aggregate(scalauto, scalandroid)
