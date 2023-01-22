package com.vxksoftware.aws

import com.amazonaws.services.sqs.model.*
import com.amazonaws.services.sqs.{AmazonSQS, AmazonSQSClient}
import com.vxksoftware.aws.{SQSClient, SQSClientLive}
import zio.*

import java.net.URL
import scala.concurrent.duration.*

trait SQSClient:
  def poll(url: URL): Task[Chunk[Message]]

object SQSClient:
  val live: URLayer[AmazonSQS, SQSClientLive] = ZLayer.fromFunction(SQSClientLive.apply _)

case class SQSClientLive(
  awsSQSClient: AmazonSQS
) extends SQSClient:

  def poll(url: URL): Task[Chunk[Message]] =
    for
      _ <- ZIO.unit
      request = ReceiveMessageRequest()
                  .withQueueUrl(url.toString)
                  .withVisibilityTimeout(5)
                  .withWaitTimeSeconds(0)
      receiveMessageResult <- ZIO.attempt(awsSQSClient.receiveMessage(request))
      messages              = Chunk.fromJavaIterable[Message](receiveMessageResult.getMessages)
    yield messages
