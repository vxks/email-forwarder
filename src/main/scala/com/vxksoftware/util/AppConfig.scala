package com.vxksoftware.util

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfigSource

case class AppConfig(
  aws: AWSConfig
)

case class AWSConfig(
  profileName: Option[String],
  sqs: SQSConfig,
  s3: S3Config,
)

case class SQSConfig(
  emailQueueUrl: java.net.URL
)

case class S3Config(
  emailBucket: String,
  emailFolder: String
)

object AppConfig:
  private lazy val config           = TypesafeConfigSource.fromResourcePath
  private lazy val configDescriptor = descriptor[AppConfig] from config

  lazy val live: ZLayer[Any, ReadError[String], AppConfig] =
    ZLayer.fromZIO(read(configDescriptor))
