import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.{
  AmazonSimpleEmailService,
  AmazonSimpleEmailServiceClient,
  AmazonSimpleEmailServiceClientBuilder
}
import com.amazonaws.services.sqs.*
import com.amazonaws.services.sqs.model.Message
import com.vxksoftware.aws.{SESClient, SQSClient}
import com.vxksoftware.http.ForwardingApp
import com.vxksoftware.util.AppConfig
import zio.*
import zio.http.*
import zio.http.model.Method

object ServerApp extends ZIOAppDefault:

  val app: HttpApp[SESClient, Throwable] = Http.collectZIO[Request] {
    case Method.GET -> !! / "send" =>
      ZIO.succeed(Response.text("Hello World!"))

    case request @ Method.POST -> !! / "send" =>
      ZIO.serviceWithZIO[SESClient] { client =>
        client.sendSimpleEmail(
          from = "admin@vxksoftware.com",
          to = "test@vxksoftware.com",
          subject = "Hey",
          text = "from server!"
        )
      } *> ZIO.succeed(Response.text("done"))
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

  val awsSQSClientLayer: URLayer[ProfileCredentialsProvider, AmazonSQS] =
    ZLayer.fromZIO {
      ZIO.serviceWith[ProfileCredentialsProvider] { credentialsProvider =>
        AmazonSQSClientBuilder
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
    (for
      _ <- Server.serve(app).fork
      _ <- ForwardingApp.start
    yield ExitCode.success).provide(
      Server.default,
      awsSESClientLayer,
      awsSQSClientLayer,
      SESClient.live,
      SQSClient.live,
      credentialsProviderLayer,
      AppConfig.live
    )
