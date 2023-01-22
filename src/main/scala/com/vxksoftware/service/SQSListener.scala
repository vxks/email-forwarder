package com.vxksoftware.service

import com.amazonaws.services.sqs.model.Message
import com.vxksoftware.aws.SQSClient
import zio.*
import zio.stream.ZStream

import java.net.URL

object SQSListener:
  def stream(url: URL, every: Duration = 10.seconds): ZStream[SQSClient, Throwable, Message] =
    ZStream
      .repeatZIOWithSchedule(
        ZIO.serviceWithZIO[SQSClient](_.poll(url)),
        Schedule.spaced(every)
      )
      .flattenChunks
