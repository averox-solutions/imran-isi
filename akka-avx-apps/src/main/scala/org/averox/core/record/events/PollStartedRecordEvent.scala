/**
 * Averox open source conferencing system - http://www.averox.org/
 *
 * Copyright (c) 2017 Averox Inc. and by respective authors (see below).
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

package org.averox.core.record.events

import org.averox.common2.domain.SimpleAnswerOutVO
import org.averox.common2.util.JsonUtil

class PollStartedRecordEvent extends AbstractPollRecordEvent {
  import PollStartedRecordEvent._

  setEvent("PollStartedRecordEvent")

  def setUserId(userId: String) {
    eventMap.put(USER_ID, userId)
  }

  def setQuestion(question: String) {
    eventMap.put(QUESTION, question)
  }

  def setAnswers(answers: Array[SimpleAnswerOutVO]) {
    eventMap.put(ANSWERS, JsonUtil.toJson(answers))
  }

  def setType(pollType: String) {
    eventMap.put(TYPE, pollType)
  }

  def setSecretPoll(secretPoll: Boolean) {
    eventMap.put(SECRET_POLL, secretPoll.toString)
  }
}

object PollStartedRecordEvent {
  protected final val USER_ID = "userId"
  protected final val QUESTION = "question"
  protected final val ANSWERS = "answers"
  protected final val TYPE = "type"
  protected final val SECRET_POLL = "secretPoll"
}