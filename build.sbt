name := """sso"""

version := "1.0"

scalaVersion := "2.11.8"


libraryDependencies ++= Seq(
  "commons-lang" % "commons-lang" % "2.6",
  "com.zeroc" % "ice" % "3.6.2",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.4",
  "com.typesafe.slick" % "slick-codegen_2.11" % "3.2.0-M1" % "test"
)

libraryDependencies += "com.lawsofnature.common" % "common-mysql_2.11" % "1.0"
libraryDependencies += "com.lawsofnature.common" % "common-ice_2.11" % "1.0"

libraryDependencies += "com.lawsofnature.member" % "memberclient_2.11" % "1.0-SNAPSHOT"
libraryDependencies += "com.lawsofnature.client" % "ssoclient_2.11" % "1.0"

libraryDependencies += "com.lawsofnature.common" % "common-error_2.11" % "1.0"
libraryDependencies += "com.lawsofnature.common" % "common-utils_2.11" % "1.0"
libraryDependencies += "com.lawsofnature.common" % "common-redis_2.11" % "1.0"
