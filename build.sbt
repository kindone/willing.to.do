name := """willing.to.do"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

resolvers ++= Seq(
	"26Lights releases" at "http://build.26source.org/nexus/content/repositories/public-releases",
	"fwbrasil.net" at "http://fwbrasil.net/maven/"
	)

libraryDependencies ++= Seq(
	jdbc,
	anorm,
	cache,
	ws,
	"org.webjars" %% "webjars-play" % "2.3.0-2",
	"26lights"  %% "playr"  % "0.4.0",
	"org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
	"net.fwbrasil" %% "activate-play" % "1.7" exclude("org.scala-stm", "scala-stm_2.10.0"),
	"net.fwbrasil" %% "activate-jdbc-async" % "1.7" exclude("org.scala-stm", "scala-stm_2.10.0"),
	"jp.t2v" %% "play2-auth" % "0.13.2",
	"de.svenkubiak" % "jBCrypt" % "0.4",
	"jp.t2v" %% "play2-auth-test" % "0.13.2" % "test",
	"org.seleniumhq.selenium" % "selenium-java" % "2.31.0",
	"org.webjars.npm" % "jquery" % "2.1.4",
	"org.webjars" % "momentjs" % "2.9.0",
	"org.webjars" % "Eonasdan-bootstrap-datetimepicker" % "4.7.14",
	"org.webjars" % "angularjs" % "1.3.15",
	"org.webjars" % "underscorejs" % "1.8.3",
	"org.webjars" % "angular-ui-tree" % "2.1.5",
	"org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
	"org.scalatestplus" %% "play" % "1.1.0" % "test"
)

parallelExecution in Test := false

parallelExecution in IntegrationTest := false
