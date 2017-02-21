lazy val commonSettings = Seq(
  javacOptions ++= Seq("-encoding", "UTF-8"),
  organization := "com.jxjxgo.sso",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "net.codingwell" % "scala-guice_2.11" % "4.1.0",
    "org.scala-lang" % "scala-library" % "2.11.8",
    "com.jxjxgo.common" % "common-finagle-thrift_2.11" % "1.0"
  )
)

lazy val ssocommonlib = (project in file("ssocommonlib")).settings(commonSettings: _*).settings(
  name := """ssocommonlib""",
  libraryDependencies ++= Seq(
  )
)

lazy val ssoserver = (project in file("ssoserver")).settings(commonSettings: _*).settings(
  name := """ssoserver""",
  mainClass in (Compile, run) := Some("com.lawsofnature.sso.service.SystemService"),
  libraryDependencies ++= Seq(
    "commons-lang" % "commons-lang" % "2.6",
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.4",
    "com.trueaccord.scalapb" % "scalapb-runtime_2.11" % "0.5.46",

    "com.jxjxgo.common" % "common-db_2.11" % "1.0",
    "com.jxjxgo.common" % "common-error_2.11" % "1.0",
    "com.jxjxgo.common" % "common-redis_2.11" % "1.0",

    "com.jxjxgo.common" % "common-utils_2.11" % "1.0",
    "com.jxjxgo.sso" % "ssocommonlib_2.11" % "1.0",
    "com.jxjxgo.member" % "membercommonlib_2.11" % "1.0"
  )
)