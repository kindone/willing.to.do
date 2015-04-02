name := """willing.to.do"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers ++= Seq(
	"26Lights releases" at "http://build.26source.org/nexus/content/repositories/public-releases",
	"fwbrasil.net" at "http://fwbrasil.net/maven/"
	)

libraryDependencies ++= Seq(
	jdbc,
	anorm,
	cache,
	ws,
	"26lights"  %% "playr"  % "0.4.0",
	"org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
	"net.fwbrasil" %% "activate-play" % "1.7" exclude("org.scala-stm", "scala-stm_2.10.0"),
	"net.fwbrasil" %% "activate-jdbc-async" % "1.7" exclude("org.scala-stm", "scala-stm_2.10.0"),
  "jp.t2v" %% "play2-auth" % "0.13.2",
  "jp.t2v" %% "play2-auth-test" % "0.13.2" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "2.31.0",
	"org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
	"org.scalatestplus" %% "play" % "1.1.0" % "test"
)

parallelExecution in Test := false

parallelExecution in IntegrationTest := false
