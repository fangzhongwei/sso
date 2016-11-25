name := """sso"""

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.lawsofnature.common" % "common-mysql_2.11" % "1.0"
libraryDependencies += "com.lawsofnature.common" % "common-ice_2.11" % "1.0"

libraryDependencies += "com.lawsofnature.member" % "memberclient_2.11" % "1.0-SNAPSHOT"
libraryDependencies += "com.lawsofnature.client" % "ssoclient_2.11" % "1.0"

libraryDependencies += "com.lawsofnature.common" % "common-error_2.11" % "1.0"
libraryDependencies += "com.lawsofnature.common" % "common-utils_2.11" % "1.0"
libraryDependencies += "com.lawsofnature.common" % "common-redis_2.11" % "1.0"



libraryDependencies += "com.typesafe.slick" % "slick-codegen_2.11" % "3.2.0-M1" % "test"