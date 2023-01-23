package com.vxksoftware.model

import zio.json.*
case class LambdaMessage(
  Records: List[Record]
)

object LambdaMessage:
  given JsonDecoder[LambdaMessage] = DeriveJsonDecoder.gen[LambdaMessage]

case class Record(
  ses: SES
)

object Record:
  given JsonDecoder[Record] = DeriveJsonDecoder.gen[Record]

case class SES(
  mail: SESMail
)

object SES:
  given JsonDecoder[SES] = DeriveJsonDecoder.gen[SES]

case class SESMail(
  source: String,
  destination: List[String], // email addresses
  messageId: String
)

object SESMail:
  given JsonDecoder[SESMail] = DeriveJsonDecoder.gen[SESMail]
