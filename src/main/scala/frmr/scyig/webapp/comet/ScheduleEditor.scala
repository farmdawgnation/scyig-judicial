package frmr.scyig.webapp.comet

import frmr.scyig.db._
import frmr.scyig.webapp.snippet.CompSchedule
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import slick.jdbc.MySQLProfile.api._

class ScheduleEditor extends CometActor with Loggable {
  private[this] val competition: Competition = CompSchedule.menu.toLoc.currentValue openOrThrowException("No competition?")
  private[this] var currentEditorMatches: Seq[Match] = CompSchedule.populatedMatches.is

  private[this] def updateMatch(index: Int, updater: (Match)=>Match): Unit = {
    val matchInQuestion = currentEditorMatches(index)
    currentEditorMatches = currentEditorMatches.patch(index, updater(matchInQuestion) :: Nil, 1)
  }

  def render = {
    val scheduledTeamIds = currentEditorMatches.flatMap { m =>
      Seq(m.prosecutionTeamId, m.defenseTeamId)
    }

    val byeTeams = DB.runAwait(Teams.to[List].filterNot(_.id inSet scheduledTeamIds).result) match {
      case Full(actualByeTeams) =>
        actualByeTeams

      case other =>
        logger.warn(s"Got unexpected result when computing bye teams: $other")
        Nil
    }

    S.appendJs(Call("window.bindSuggestions").cmd)

    ClearClearable andThen
    "^ [data-rerender-function-id]" #> SHtml.ajaxInvoke( () => reRender()).guid &
    "^ [data-competition-id]" #> competition.id.getOrElse(0).toString &
    ".match-row" #> currentEditorMatches.zipWithIndex.flatMap {
      case (m, idx) =>
        for {
          prosecution <- DB.runAwait(Teams.filter(_.id === m.prosecutionTeamId).result.head)
          defense <- DB.runAwait(Teams.filter(_.id === m.defenseTeamId).result.head)
          presidingJudge <- DB.runAwait(Judges.filter(_.id === m.presidingJudgeId).result.head)
          scoringJudge <- DB.runAwait(Judges.filter(_.id === m.scoringJudgeId.getOrElse(-1)).result.head)
        } yield {
          ".prosecution-team-id" #> SHtml.hidden(v => updateMatch(idx, _.copy(prosecutionTeamId = v.toInt)), m.prosecutionTeamId.toString) &
          ".prosecution-team [value]" #> prosecution.name &
          ".defense-team-id" #> SHtml.hidden(v => updateMatch(idx, _.copy(defenseTeamId = v.toInt)), m.defenseTeamId.toString) &
          ".defense-team [value]" #> defense.name &
          ".presiding-judge-id" #> SHtml.hidden(v => updateMatch(idx, _.copy(presidingJudgeId = v.toInt)), m.presidingJudgeId.toString) &
          ".presiding-judge [value]" #> presidingJudge.name &
          ".scoring-judge-id" #> SHtml.hidden(v => updateMatch(idx, _.copy(scoringJudgeId = Some(v.toInt))), m.scoringJudgeId.toString) &
          ".scoring-judge [value]" #> scoringJudge.name
        }
    } &
    ".bye-team" #> byeTeams.map { team =>
      "^ *" #> team.name
    }
  }
}