package com.vxksoftware.model

import com.amazonaws.services.simpleemail.model.*

case class Mail(
  from: String,
  to: String,
  subject: String,
  body: String
)