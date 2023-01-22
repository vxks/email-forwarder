package com.vxksoftware.aws

import com.amazonaws.services.simpleemail
import com.amazonaws.services.simpleemail.model.{Body as MessageBody, *}
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailService, AmazonSimpleEmailServiceClient}
import com.vxksoftware.aws.{SESClient, SESClientLive}
import org.apache.http.client.CredentialsProvider
import zio.*
import zio.http.*

trait SESClient:
  def sendRawEmail(request: SendRawEmailRequest): Task[SendRawEmailResult]
  def sendEmail(request: SendEmailRequest): Task[SendEmailResult]
  def sendSimpleEmail(subject: String, from: String, to: String, text: String): Task[SendEmailResult]

object SESClient:
  val live: URLayer[AmazonSimpleEmailService, SESClientLive] = ZLayer.fromFunction(SESClientLive.apply _)

case class SESClientLive(
  awsClient: AmazonSimpleEmailService
) extends SESClient:

  override def sendRawEmail(request: SendRawEmailRequest): Task[SendRawEmailResult] =
    ZIO.attempt(awsClient.sendRawEmail(request))

  override def sendEmail(request: SendEmailRequest): Task[SendEmailResult] =
    ZIO.attempt(awsClient.sendEmail(request))

  override def sendSimpleEmail(subject: String, from: String, to: String, text: String): Task[SendEmailResult] = {
    val destination    = new Destination().withToAddresses(to)
    val subjectContent = new Content(subject)
    val bodyContent    = new Content(text)
    val body           = new MessageBody(bodyContent)
    val message        = new Message(subjectContent, body)
    val request        = new SendEmailRequest(from, destination, message)
    sendEmail(request)
  }
