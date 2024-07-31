import org.averox.build._

import NativePackagerHelper._
import com.typesafe.sbt.SbtNativePackager.autoImport._

enablePlugins(JavaServerAppPackaging)
enablePlugins(UniversalPlugin)
enablePlugins(DebianPlugin)

version := "0.0.4"

val compileSettings = Seq(
  organization := "org.averox",

  scalacOptions ++= List(
    "-unchecked",
    "-deprecation",
    "-Xlint",
    "-Ywarn-dead-code",
    "-language:_",
    "-release:17",
    "-encoding", "UTF-8"
  ),
  javacOptions ++= List(
    "-Xlint:unchecked",
    "-Xlint:deprecation"
  )
)

publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/dev/repo/maven-repo/releases")))

// We want to have our jar files in lib_managed dir.
// This way we'll have the right path when we import
// into eclipse.
retrieveManaged := true

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.0"

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "html", "console", "junitxml")

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/scalatest-reports")

Seq(Revolver.settings: _*)
lazy val avxAppsAkka = (project in file(".")).settings(name := "avx-apps-akka", libraryDependencies ++= Dependencies.runtime).settings(compileSettings)

// See https://github.com/scala-ide/scalariform
// Config file is in ./.scalariform.conf
scalariformAutoformat := true

scalaVersion := "2.13.9"
//-----------
// Packaging
//
// Reference:
// https://github.com/muuki88/sbt-native-packager-examples/tree/master/akka-server-app
// http://www.scala-sbt.org/sbt-native-packager/index.html
//-----------
mainClass := Some("org.averox.Boot")

maintainer in Linux := "Richard Alam <ritzalam@gmail.com>"

packageSummary in Linux := "Averox Apps (Akka)"

packageDescription := """Averox Core Apps in Akka."""

val user = "averox"

val group = "averox"

// user which will execute the application
daemonUser in Linux := user

// group which will execute the application
daemonGroup in Linux := group

javaOptions in Universal ++= Seq("-J-Xms130m", "-J-Xmx256m", "-Dconfig.file=/etc/averox/avx-apps-akka.conf", "-Dlogback.configurationFile=conf/logback.xml")
javaOptions in reStart ++= Seq("-Dconfig.file=/etc/averox/avx-apps-akka.conf", "-Dlogback.configurationFile=conf/logback.xml")

debianPackageDependencies in Debian ++= Seq("java17-runtime-headless", "bash")