package org.averox.core.record.events

class RecordChapterBreakRecordEvent extends AbstractParticipantRecordEvent {

  setEvent("RecordChapterBreakEvent")

  def setChapterBreakTimestamp(timestamp: Long) {
    eventMap.put("breakTimestamp", timestamp.toString)
  }
}
