package org.averox.web

class UrlMappings {

  static mappings = {
    "/averox/presentation/$authzToken/upload"(controller: "presentation") {
      action = [POST: 'upload']
    }

    "/averox/presentation/checkPresentation"(controller: "presentation") {
      action = [GET: 'checkPresentationBeforeUploading']
    }

    "/averox/presentation/test-convert"(controller: "presentation") {
      action = [GET: 'testConversion']
    }

    "/averox/presentation/$conference/$room/$presentation_name/slides"(controller: "presentation") {
      action = [GET: 'numberOfSlides']
    }

    "/averox/presentation/$conference/$room/$presentation_name/thumbnails"(controller: "presentation") {
      action = [GET: 'numberOfThumbnails']
    }

    "/averox/presentation/$conference/$room/$presentation_name/thumbnail/$id"(controller: "presentation") {
      action = [GET: 'showThumbnail']
      constraints {
        id matches: /\d+/
      }
    }

    "/averox/presentation/$conference/$room/$presentation_name/png/$id"(controller: "presentation") {
      action = [GET: 'showPng']
      constraints {
        id matches: /\d+/
      }
    }

    "/averox/presentation/$conference/$room/$presentation_name/svgs"(controller: "presentation") {
      action = [GET: 'numberOfSvgs']
    }

    "/averox/presentation/$conference/$room/$presentation_name/svg/$id"(controller: "presentation") {
      action = [GET: 'showSvgImage']
      constraints {
        id matches: /\d+/
      }
    }

    "/averox/presentation/$conference/$room/$presentation_name/textfiles"(controller: "presentation") {
      action = [GET: 'numberOfTextfiles']
    }

    "/averox/presentation/$conference/$room/$presentation_name/textfiles/$id"(controller: "presentation") {
      action = [GET: 'showTextfile']
      constraints {
        id matches: /\d+/
      }
    }

    "/averox/presentation/download/$meetingId/$presId"(controller: "presentation") {
      action = [GET: 'downloadFile']
    }

    "/averox/api/create"(controller: "api") {
      action = [GET: 'create', POST: 'create']
    }

    "/averox/api/join"(controller: "api") {
      action = [GET: 'join']
    }

    "/averox/api/isMeetingRunning"(controller: "api") {
      action = [GET: 'isMeetingRunning', POST: 'isMeetingRunning']
    }

    "/averox/api/end"(controller: "api") {
      action = [GET: 'end', POST: 'end']
    }

    "/averox/api/getMeetingInfo"(controller: "api") {
      action = [GET: 'getMeetingInfo', POST: 'getMeetingInfo']
    }

    "/averox/api/getMeetings"(controller: "api") {
      action = [GET: 'getMeetingsHandler', POST: 'getMeetingsHandler']
    }

    "/averox/api/getSessions"(controller: "api") {
      action = [GET: 'getSessionsHandler', POST: 'getSessionsHandler']
    }

    "/averox/api/stuns"(controller: "api") {
      action = [GET: 'stuns', POST: 'stuns']
    }

    "/averox/api/signOut"(controller: "api") {
      action = [GET: 'signOut', POST: 'signOut']
    }

    "/averox/api/insertDocument"(controller: "api") {
      action = [POST: 'insertDocument']
    }

    "/averox/api/getJoinUrl"(controller: "api") {
      action = [GET: 'getJoinUrl', POST: 'getJoinUrl']
    }

    "/averox/api/feedback"(controller: "api") {
      action = [POST: 'feedback']
    }

    "/averox/api/learningDashboard"(controller: "api") {
      action = [GET: 'learningDashboard', POST: 'learningDashboard']
    }


    "/averox/api/sendChatMessage"(controller: "api") {
      action = [GET: 'sendChatMessage']
    }

    "/averox/api/getRecordings"(controller: "recording") {
      action = [GET: 'getRecordingsHandler', POST: 'getRecordingsHandler']
    }

    "/averox/api/updateRecordings"(controller: "recording") {
      action = [GET: 'updateRecordingsHandler', POST: 'updateRecordingsHandler']
    }

    "/averox/api/guestWait"(controller: "api") {
      action = [GET: 'guestWaitHandler']
    }

    "/averox/textTrack/validateAuthToken"(controller: "recording") {
      action = [GET: 'checkTextTrackAuthToken']
    }

    "/averox/api/getRecordingTextTracks"(controller: "recording") {
      action = [GET: 'getRecordingTextTracksHandler', POST: 'getRecordingTextTracksHandler']
    }

    "/averox/api/putRecordingTextTrack"(controller: "recording") {
      action = [POST: 'putRecordingTextTrack']
    }

    "/averox/api/publishRecordings"(controller: "recording") {
      action = [GET: 'publishRecordings']
    }

    "/averox/api/deleteRecordings"(controller: "recording") {
      action = [GET: 'deleteRecordings']
    }

    "/averox/$controller/$action?/$id?(.${format})?" {
      constraints {
        // apply constraints here
      }
    }

    "/averox/"(controller: "api") {
      action = [GET: 'index']
    }

    "500"(view: '/error')
  }
}
