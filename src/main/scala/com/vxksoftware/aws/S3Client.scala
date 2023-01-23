package com.vxksoftware.aws

import com.amazonaws.services.s3.AmazonS3
import com.vxksoftware.util.AppConfig
import zio.*
import zio.stream.{ZSink, ZStream}

import java.io.{File, FileOutputStream}

trait S3Client:
  def downloadObject(objectPath: String, to: File): Task[Unit]

object S3Client:
  val live: URLayer[AmazonS3 & AppConfig, S3Client] =
    ZLayer.fromZIO {
      for
        awsClient <- ZIO.service[AmazonS3]
        appConfig <- ZIO.service[AppConfig]
      yield S3ClientLive(awsClient, appConfig)
    }

case class S3ClientLive(
  awsClient: AmazonS3,
  appConfig: AppConfig
) extends S3Client:
  def downloadObject(objectKey: String, to: File): Task[Unit] =
    for
      s3Object <- ZIO.attempt(awsClient.getObject(appConfig.aws.s3.emailBucket, objectKey))
      outStream = ZStream.fromInputStreamZIO(ZIO.attemptBlockingIO(s3Object.getObjectContent.getDelegateStream))
      fileSink  = ZSink.fromFile(to)
      _        <- outStream.run(fileSink)
    yield ()
