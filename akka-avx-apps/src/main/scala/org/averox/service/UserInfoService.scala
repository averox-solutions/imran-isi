package org.averox.service

import org.apache.pekko.actor.{ActorRef, ActorSystem}
import org.apache.pekko.http.scaladsl.model.headers.RawHeader
import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCode}
import org.apache.pekko.pattern.ask
import org.apache.pekko.pattern.AskTimeoutException
import org.apache.pekko.util.Timeout
import org.averox.common2.util.JsonUtil
import org.averox.core.api.{ApiResponse, ApiResponseFailure, GetUserApiMsg, UserInfosApiMsg}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}


object UserInfoService {
  def apply(system: ActorSystem, avxActor: ActorRef) = new UserInfoService(system, avxActor)
}

class UserInfoService(system: ActorSystem, avxActor: ActorRef) {
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = 2 seconds

  def getUserInfo(sessionToken: String): Future[ApiResponse] = {
    val future = avxActor.ask(GetUserApiMsg(sessionToken)).mapTo[ApiResponse]

    future.recover {
      case e: AskTimeoutException => ApiResponseFailure("Request Timeout error")
    }
  }

  def generateResponseMap(userInfos: UserInfosApiMsg): Map[String, Any] = {
    val infos = userInfos.infos
    val meetingID = infos.getOrElse("meetingID", "").toString
    val userId = infos.getOrElse("internalUserID", "").toString

    def conditionalValue(key: String, defaultValueTrue: String, defaultValueFalse: String): String = {
      infos.get(key) match {
        case Some(value: Boolean) if value => defaultValueTrue
        case _                             => defaultValueFalse
      }
    }

    if (conditionalValue("online", "true", "false") == "true") {
      Map(
        "response" -> "authorized",
        "X-Hasura-Role" -> "avx_client",
        "X-Hasura-ModeratorInMeeting" -> conditionalValue("moderator", meetingID, ""),
        "X-Hasura-PresenterInMeeting" -> conditionalValue("presenter", meetingID, ""),
        "X-Hasura-UserId" -> userId,
        "X-Hasura-MeetingId" -> meetingID,
        "X-Hasura-CursorNotLockedInMeeting" -> conditionalValue("hideViewersCursor", "", meetingID),
        "X-Hasura-CursorLockedUserId" -> conditionalValue("hideViewersCursor", userId, ""),
        "X-Hasura-AnnotationsNotLockedInMeeting" -> conditionalValue("hideViewersAnnotation", "", meetingID),
        "X-Hasura-AnnotationsLockedUserId" -> conditionalValue("hideViewersAnnotation", userId, ""),
        "X-Hasura-UserListNotLockedInMeeting" -> conditionalValue("hideUserList", "", meetingID),
        "X-Hasura-WebcamsNotLockedInMeeting" -> conditionalValue("webcamsOnlyForModerator", "", meetingID),
        "X-Hasura-WebcamsLockedUserId" -> conditionalValue("webcamsOnlyForModerator", userId, "")
      )
    } else {
      Map(
        "response" -> "authorized",
        "X-Hasura-Role" -> "not_joined_avx_client",
        "X-Hasura-ModeratorInMeeting" -> conditionalValue("moderator", meetingID, ""),
        "X-Hasura-PresenterInMeeting" -> conditionalValue("presenter", meetingID, ""),
        "X-Hasura-UserId" -> userId,
        "X-Hasura-MeetingId" -> meetingID,
      )
    }

  }

  def createHttpResponse(status: StatusCode, response: Map[String, Any]): HttpResponse = {
    HttpResponse(
      status,
      headers = Seq(RawHeader("Cache-Control", "no-cache")),
      entity = HttpEntity(ContentTypes.`application/json`, JsonUtil.toJson(response))
    )
  }

}
