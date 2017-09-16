package frmr.scyig.db

import slick.jdbc.MySQLProfile.api._

sealed trait CompetitionStatus {
  def value: String
}
case object NotStarted extends CompetitionStatus {
  val value = "Not Started"
}
case object InProgress extends CompetitionStatus {
  val value = "In Progress"
}
case object Finished extends CompetitionStatus {
  val value = "Finished"
}

object CompetitionStatus {
  def forValue(input: String): CompetitionStatus = input match {
    case NotStarted.value => NotStarted
    case InProgress.value => InProgress
    case Finished.value => Finished
    case s => throw new IllegalStateException(s"Illegal competition status: $s")
  }

  implicit val competitionStatusColumnType = MappedColumnType.base[CompetitionStatus, String](
    _.value,
    forValue(_)
  )
}

case class Competition(
  id: Option[Int],
  name: String,
  sponsorId: Int,
  dates: String,
  description: String,
  location: String,
  status: CompetitionStatus = NotStarted,
  round: Int = 0
)

class Competitions(tag: Tag) extends Table[Competition](tag, "competitions") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def sponsorId = column[Int]("sponsor_id")
  def dates = column[String]("dates")
  def description = column[String]("description")
  def location = column[String]("location")
  def status = column[CompetitionStatus]("status")
  def round = column[Int]("round")

  def * = (id.?, name, sponsorId, dates, description, location, status, round) <> (Competition.tupled, Competition.unapply)

  def sidFK = foreignKey("c_sponsor_id_fk", sponsorId, Sponsors)(_.id, onDelete = ForeignKeyAction.Cascade)
}

object Competitions extends TableQuery[Competitions](new Competitions(_))
