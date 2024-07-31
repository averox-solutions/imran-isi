package org.averox.core.db

import org.averox.core.models.{ WebcamStream}
import slick.jdbc.PostgresProfile.api._

case class UserCameraDbModel(
        streamId:      String,
        meetingId:     String,
        userId:        String,
)

class UserCameraDbTableDef(tag: Tag) extends Table[UserCameraDbModel](tag, None, "user_camera") {
  override def * = (
    streamId, meetingId, userId) <> (UserCameraDbModel.tupled, UserCameraDbModel.unapply)
  val streamId = column[String]("streamId", O.PrimaryKey)
  val meetingId = column[String]("meetingId")
  val userId = column[String]("userId")
}

object UserCameraDAO {

  def insert(meetingId: String, webcam: WebcamStream) = {
    DatabaseConnection.enqueue(
      TableQuery[UserCameraDbTableDef].forceInsert(
        UserCameraDbModel(
          streamId = webcam.streamId,
          meetingId = meetingId,
          userId = webcam.userId,
        )
      )
    )
  }

  def delete(streamId: String) = {
    DatabaseConnection.enqueue(
      TableQuery[UserCameraDbTableDef]
        .filter(_.streamId === streamId)
        .delete
    )
  }


}
