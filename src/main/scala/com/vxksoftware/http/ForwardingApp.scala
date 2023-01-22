package com.vxksoftware.http

import com.amazonaws.services.sqs.model.Message
import com.vxksoftware.aws.SQSClient
import com.vxksoftware.service.SQSListener
import com.vxksoftware.util.AppConfig
import zio.*
import zio.json.*
import zio.stream.*

import java.net.URL

object ForwardingApp:
  def start: ZIO[SQSClient with AppConfig, Throwable, Unit] =
    for
      _            <- ZIO.log("Starting forwarding app")
      config       <- ZIO.service[AppConfig]
      url           = config.aws.sqs.emailQueueUrl
      messageStream = SQSListener.stream(url)
      _            <- messageStream.runForeach(processMessage)
      _            <- ZIO.never
    yield ()

def processMessage(message: Message) =
  ZIO.log(s"Processing SQS message ${message.getMessageId}") *>
    ZIO.log(message.getBody)
