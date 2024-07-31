package org.averox.core.db

import org.averox.common2.domain.{ VoiceProp }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ ProvenShape }

case class MeetingVoiceDbModel(
    meetingId:   String,
    telVoice:    String,
    voiceConf:   String,
    dialNumber:  String,
    muteOnStart: Boolean
)

class MeetingVoiceDbTableDef(tag: Tag) extends Table[MeetingVoiceDbModel](tag, "meeting_voice") {
  val meetingId = column[String]("meetingId", O.PrimaryKey)
  val telVoice = column[String]("telVoice")
  val voiceConf = column[String]("voiceConf")
  val dialNumber = column[String]("dialNumber")
  val muteOnStart = column[Boolean]("muteOnStart")

  //  def fk_meetingId: ForeignKeyQuery[MeetingDbTableDef, MeetingDbModel] = foreignKey("fk_meetingId", meetingId, TableQuery[MeetingDbTableDef])(_.meetingId)

  override def * : ProvenShape[MeetingVoiceDbModel] = (meetingId, telVoice, voiceConf, dialNumber, muteOnStart) <> (MeetingVoiceDbModel.tupled, MeetingVoiceDbModel.unapply)
}

object MeetingVoiceDAO {
  def insert(meetingId: String, voiceProp: VoiceProp) = {
    DatabaseConnection.enqueue(
      TableQuery[MeetingVoiceDbTableDef].forceInsert(
        MeetingVoiceDbModel(
          meetingId = meetingId,
          telVoice = voiceProp.telVoice,
          voiceConf = voiceProp.voiceConf,
          dialNumber = voiceProp.dialNumber,
          muteOnStart = voiceProp.muteOnStart
        )
      )
    )
  }
}