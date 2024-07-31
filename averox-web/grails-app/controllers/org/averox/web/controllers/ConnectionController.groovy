/**
* Averox open source conferencing system - http://www.averox.org/
*
* Copyright (c) 2019 Averox Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 3.0 of the License, or (at your option) any later
* version.
*
* Averox is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with Averox; if not, see <http://www.gnu.org/licenses/>.
*
*/
package org.averox.web.controllers

import groovy.json.JsonBuilder
import org.averox.api.MeetingService
import org.averox.api.domain.Meeting
import org.averox.api.domain.UserSession
import org.averox.api.domain.User
import org.averox.api.domain.UserSessionBasicData
import org.averox.api.util.ParamsUtil
import org.averox.api.ParamsProcessorUtil
import java.nio.charset.StandardCharsets


class ConnectionController {
  MeetingService meetingService
  ParamsProcessorUtil paramsProcessorUtil

  def checkAuthorization = {
    try {
      def uri = request.getHeader("x-original-uri")
      def sessionToken = ParamsUtil.getSessionToken(uri)
      UserSession userSession = meetingService.getUserSessionWithSessionToken(sessionToken)
      Boolean allowRequestsWithoutSession = meetingService.getAllowRequestsWithoutSession(sessionToken)
      Boolean isSessionTokenInvalid = !session[sessionToken] && !allowRequestsWithoutSession

      response.addHeader("Cache-Control", "no-cache")
      response.contentType = 'plain/text'

      if (userSession != null && !isSessionTokenInvalid) {
        response.addHeader("User-Id", userSession.internalUserId)
        response.addHeader("Meeting-Id", userSession.meetingID)
        response.addHeader("Voice-Bridge", userSession.voicebridge )
        response.addHeader("User-Name", URLEncoder.encode(userSession.fullname, StandardCharsets.UTF_8.name()))
        response.setStatus(200)
        response.outputStream << 'authorized'
      } else {
        response.setStatus(401)
        response.outputStream << 'unauthorized'
      }
    } catch (IOException e) {
      log.error("Error while authenticating connection.\n" + e.getMessage())
    }
  }

  def checkGraphqlAuthorization = {
    try {
      String sessionToken = request.getHeader("x-session-token")

      UserSession userSession = meetingService.getUserSessionWithSessionToken(sessionToken)
      Boolean isSessionTokenValid = session[sessionToken] != null

      response.addHeader("Cache-Control", "no-cache")

      if (userSession != null && isSessionTokenValid) {
        Meeting m = meetingService.getMeeting(userSession.meetingID)
        User u
        if(m) {
          u = m.getUserById(userSession.internalUserId)
        }

        response.addHeader("Meeting-Id", userSession.meetingID)
        response.setStatus(200)
        withFormat {
          json {
            def builder = new JsonBuilder()
            builder {
              "response" "authorized"
              "X-Currently-Online" m && u && !u.hasLeft() ? "true" : "false"
              "X-Moderator" u && u.isModerator() ? "true" : "false"
              "X-Presenter" u && u.isPresenter() ? "true" : "false"
              "X-UserId" userSession.internalUserId
              "X-MeetingId" userSession.meetingID
            }
            render(contentType: "application/json", text: builder.toPrettyString())
          }
        }
      } else if(isSessionTokenValid) {
        UserSessionBasicData removedUserSession = meetingService.getRemovedUserSessionWithSessionToken(sessionToken)
        if(removedUserSession) {
          response.addHeader("Meeting-Id", removedUserSession.meetingId)
          response.setStatus(200)
          withFormat {
            json {
              def builder = new JsonBuilder()
              builder {
                "response" "authorized"
                "X-Currently-Online" "false"
                "X-Moderator" removedUserSession.isModerator()  ? "true" : "false"
                "X-Presenter" "false"
                "X-UserId" removedUserSession.userId
                "X-MeetingId" removedUserSession.meetingId
              }
              render(contentType: "application/json", text: builder.toPrettyString())
            }
          }
        } else {
          throw new Exception("Invalid User Session")
        }
      } else {
        throw new Exception("Invalid sessionToken")
      }
    } catch (Exception e) {
      log.debug("Error while authenticating graphql connection: " + e.getMessage())
      response.setStatus(401)
      withFormat {
        json {
          def builder = new JsonBuilder()
          builder {
            "response" "unauthorized"
          }
          render(contentType: "application/json", text: builder.toPrettyString())
        }
      }
    }
  }

  def legacyCheckAuthorization = {
    try {
      def uri = request.getHeader("x-original-uri")
      def sessionToken = ParamsUtil.getSessionToken(uri)
      UserSession userSession = meetingService.getUserSessionWithSessionToken(sessionToken)

      response.addHeader("Cache-Control", "no-cache")
      response.contentType = 'plain/text'
      if (userSession != null) {
        response.setStatus(200)
        response.outputStream << 'authorized'
      } else {
        response.setStatus(401)
        response.outputStream << 'unauthorized'
      }
    } catch (IOException e) {
      log.error("Error while authenticating connection.\n" + e.getMessage())
    }
  }
}
