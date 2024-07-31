package org.averox.core.db

import org.averox.core.domain.BreakoutRoom2x
import slick.jdbc.PostgresProfile.api._

case class UserBreakoutRoomDbModel(
        breakoutRoomId:   String,
        meetingId:        String,
        userId:           String,
        isDefaultName:    Boolean,
        sequence:         Int,
        shortName:        String,
        currentlyInRoom:  Boolean,
)

class UserBreakoutRoomDbTableDef(tag: Tag) extends Table[UserBreakoutRoomDbModel](tag, None, "user_breakoutRoom") {
  override def * = (
    breakoutRoomId, meetingId, userId, isDefaultName, sequence, shortName, currentlyInRoom) <> (UserBreakoutRoomDbModel.tupled, UserBreakoutRoomDbModel.unapply)
  val meetingId = column[String]("meetingId", O.PrimaryKey)
  val userId = column[String]("userId", O.PrimaryKey)
  val breakoutRoomId = column[String]("breakoutRoomId")
  val isDefaultName = column[Boolean]("isDefaultName")
  val sequence = column[Int]("sequence")
  val shortName = column[String]("shortName")
  val currentlyInRoom = column[Boolean]("currentlyInRoom")
}

object UserBreakoutRoomDAO {

  def updateLastBreakoutRoom(meetingId: String, userId: String, breakoutRoom: BreakoutRoom2x) = {
    DatabaseConnection.enqueue(
      TableQuery[UserBreakoutRoomDbTableDef].insertOrUpdate(
        UserBreakoutRoomDbModel(
          meetingId = meetingId,
          userId = userId,
          breakoutRoomId = breakoutRoom.id,
          isDefaultName = breakoutRoom.isDefaultName,
          sequence = breakoutRoom.sequence,
          shortName = breakoutRoom.shortName,
          currentlyInRoom = true
        )
      )
    )
  }

  def updateLastBreakoutRoom(meetingId:String, usersInRoom: Vector[String], breakoutRoom: BreakoutRoom2x) = {

    DatabaseConnection.enqueue(
      TableQuery[UserBreakoutRoomDbTableDef]
        .filter(_.meetingId === meetingId)
        .filterNot(_.userId inSet usersInRoom)
        .filter(_.breakoutRoomId === breakoutRoom.id)
        .map(u_bk => u_bk.currentlyInRoom)
        .update(false)
    )

    DatabaseConnection.enqueue(DBIO.sequence(
      for {
        userId <- usersInRoom
      } yield {
        TableQuery[UserBreakoutRoomDbTableDef].insertOrUpdate(
          UserBreakoutRoomDbModel(
            meetingId = meetingId,
            userId = userId,
            breakoutRoomId = breakoutRoom.id,
            isDefaultName = breakoutRoom.isDefaultName,
            sequence = breakoutRoom.sequence,
            shortName = breakoutRoom.shortName,
            currentlyInRoom = true
          )
        )
      }
    ).transactionally)
  }
}
