import sbt._

object Dependencies {

  lazy val aws: Set[ModuleID] = {
    val awsVersion: String = "1.12.385"
    // bc Java 9+
    val jaxb = "javax.xml.bind" % "jaxb-api" % "2.3.1"

    Set(
      "com.amazonaws" % "aws-java-sdk"      % awsVersion,
      "com.amazonaws" % "aws-java-sdk-core" % awsVersion,
      "com.amazonaws" % "aws-java-sdk-ses"  % awsVersion,
      "com.amazonaws" % "aws-java-sdk-sqs"  % awsVersion,
      jaxb
    )
  }

  lazy val zio: ModuleID     = "dev.zio" %% "zio"      % "2.0.6"
  lazy val zioHttp: ModuleID = "dev.zio" %% "zio-http" % "0.0.3"

  lazy val zioConfig: Set[ModuleID] = {
    val zioConfigVersion = "3.0.7"
    Set(
      "dev.zio" %% "zio-config"          % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion
    )
  }

  lazy val zioJson = "dev.zio" %% "zio-json" % "0.4.2"

  lazy val javaxMail: ModuleID = "com.sun.mail" % "javax.mail" % "1.6.2"

  lazy val rootDependencies: Seq[ModuleID] = (
    aws ++ zioConfig +
      zio +
      zioHttp +
      zioJson +
      javaxMail
  ).toSeq

}
