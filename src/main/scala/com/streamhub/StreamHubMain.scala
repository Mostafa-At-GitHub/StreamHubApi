package com.streamhub

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object StreamHubMain extends App with BrowsingController {

  implicit val actorSystem: ActorSystem = ActorSystem("AkkaHTTPExampleServices")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val apiRoutes: Route = pathPrefix("api") {
    reportRoutes
  }

  Http().bindAndHandle(apiRoutes, "localhost", 9999)
  logger.info("Starting the HTTP server at 9999")
  Await.result(actorSystem.whenTerminated, Duration.Inf)
}
