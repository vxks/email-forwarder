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
  sqs: SQSConfig
)

case class SQSConfig(
  emailQueueUrl: java.net.URL
)

object AppConfig:
  private lazy val config           = TypesafeConfigSource.fromResourcePath
  private lazy val configDescriptor = descriptor[AppConfig] from config

  lazy val live: ZLayer[Any, ReadError[String], AppConfig] =
    ZLayer.fromZIO(read(configDescriptor))
