package com.vxksoftware.service

import com.vxksoftware.aws.SESClient
import com.vxksoftware.model.Mail
import zio.*

trait EmailForwardingService:
  def forward(mail: Mail, to: String): Task[Unit]

case class EmailForwardingServiceLive(
  sesClient: SESClient
) extends EmailForwardingService:
  def forward(mail: Mail, to: String): Task[Unit] =
    // send email with new reply-to, etc.
    ???
