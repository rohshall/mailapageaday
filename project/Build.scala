import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "mailapageaday"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "com.typesafe.play" %% "play-slick" % "0.3.2",
      "org.apache.pdfbox" % "pdfbox" % "1.8.1",
      "org.apache.pdfbox" % "fontbox" % "1.8.1",
      "com.typesafe" %% "play-plugins-mailer" % "2.1-SNAPSHOT"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
