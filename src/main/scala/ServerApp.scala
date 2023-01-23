import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Builder, AmazonS3ClientBuilder}
import com.amazonaws.services.simpleemail.{
  AmazonSimpleEmailService,
  AmazonSimpleEmailServiceClient,
  AmazonSimpleEmailServiceClientBuilder
}
import com.amazonaws.services.sqs.*
import com.amazonaws.services.sqs.model.Message
import com.vxksoftware.aws.{S3Client, SESClient, SQSClient}
import com.vxksoftware.http.ForwardingApp
import com.vxksoftware.model.*
import com.vxksoftware.service.EmailForwardingService
import com.vxksoftware.util.AppConfig
import zio.*
import zio.http.*
import zio.http.model.Method
import zio.json.*

import java.io.File

object ServerApp extends ZIOAppDefault:

  val app: Http[AppConfig & SESClient & S3Client, Throwable, Request, Response] = Http.collectZIO[Request] {
    case request @ Method.POST -> !! / "alert" =>
      for
        appConfig     <- ZIO.service[AppConfig]
        s3Client      <- ZIO.service[S3Client]
        bodyAsString  <- request.body.asString
        lambdaMessage <- ZIO.fromEither(bodyAsString.fromJson[LambdaMessage]).mapError(new RuntimeException(_))
        _             <- ZIO.log(lambdaMessage.toString)
        objectFolder   = appConfig.aws.s3.emailFolder
        objectName     = lambdaMessage.Records.head.ses.mail.messageId
        objectPath     = objectFolder + objectName
        file          <- ZIO.attempt(new File(s"tmp/$objectName"))
        _             <- s3Client.downloadObject(objectPath, file)
      yield Response.ok
  }

  val awsSESClientLayer: URLayer[ProfileCredentialsProvider, AmazonSimpleEmailService] =
    ZLayer.fromZIO {
      ZIO.serviceWith[ProfileCredentialsProvider] { credentialsProvider =>
        AmazonSimpleEmailServiceClientBuilder
          .standard()
          .withRegion(Regions.US_WEST_2)
          .withCredentials(credentialsProvider)
          .build()
      }
    }

  val awsS3ClientLayer: URLayer[ProfileCredentialsProvider, AmazonS3] =
    ZLayer.fromZIO {
      ZIO.serviceWith[ProfileCredentialsProvider] { credentialsProvider =>
        AmazonS3ClientBuilder
          .standard()
          .withRegion(Regions.US_WEST_2)
          .withCredentials(credentialsProvider)
          .build()
      }
    }

  val credentialsProviderLayer: URLayer[AppConfig, ProfileCredentialsProvider] =
    ZLayer.fromZIO {
      ZIO.serviceWith[AppConfig] { config =>
        new ProfileCredentialsProvider(config.aws.profileName.getOrElse("default"))
      }
    }

  override def run =
    (
      for _ <- Server.serve(app)
      yield ExitCode.success
    ).provide(
      Server.default,
      awsSESClientLayer,
      awsS3ClientLayer,
      SESClient.live,
      credentialsProviderLayer,
      AppConfig.live,
      S3Client.live
    )
