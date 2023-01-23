package com.vxksoftware.service

import com.amazonaws.services.simpleemail.model.SendEmailResult
import com.vxksoftware.aws.SESClient
import com.vxksoftware.model.Mail
import zio.*

trait EmailForwardingService:
  def forward(mail: Mail, to: String): Task[SendEmailResult]

object EmailForwardingService:
  val live: URLayer[SESClient, EmailForwardingService] =
    ZLayer.fromFunction(EmailForwardingServiceLive.apply _)

case class EmailForwardingServiceLive(
  sesClient: SESClient
) extends EmailForwardingService:
  def forward(mail: Mail, to: String): Task[SendEmailResult] =
    ZIO.logInfo(s"Forwarding email from ${mail.from} to $to") *>
      sesClient.sendSimpleEmail(
        subject = mail.subject,
        from = mail.from,
        to = to,
        text = mail.body
      )
