/**
 * Copyright 2016 Matthew Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
**/
package frmr.scyig.matching

import frmr.scyig.Generators._
import frmr.scyig.matching.models._
import java.util.UUID
import org.scalacheck._
import org.scalacheck.rng._
import org.scalatest._

class MatchingPolicySpec extends WordSpec with Matchers {
  "NotFromSameOrganizationPolicy" should {
    val policyUnderTest = NotFromSameOrganizationPolicy

    "only permit other teams from different organizations" in {
      val team1 = CompetingTeam(ParticipantName("Riverside 1"), ParticipantOrganization("Riverside"))
      val team2 = CompetingTeam(ParticipantName("Riverside 2"), ParticipantOrganization("Riverside"))
      val team3 = CompetingTeam(ParticipantName("Eastside 1"), ParticipantOrganization("Eastside"))

      policyUnderTest.isValid(MatchSeed(team1), team2) should equal(false)
      policyUnderTest.isValid(MatchSeed(team2), team1) should equal(false)
      policyUnderTest.isValid(MatchSeed(team1), team3) should equal(true)
      policyUnderTest.isValid(MatchSeed(team3), team1) should equal(true)
      policyUnderTest.isValid(MatchSeed(team3), team2) should equal(true)
    }

    "only permit presiding judges without organization or from different organizations" in {
      val team1 = CompetingTeam(ParticipantName("Riverside 1"), ParticipantOrganization("Riverside"))
      val team2 = CompetingTeam(ParticipantName("Eastside 1"), ParticipantOrganization("Eastside"))
      val matchedTeams = MatchedTeams(team1, team2)

      val judge1 = Judge(ParticipantName("Bob Jones"), None, isPresiding = true)
      val judge2 = Judge(ParticipantName("Mark Appleseed"), Some(ParticipantOrganization("Eastside")), isPresiding = true)
      val judge3 = Judge(ParticipantName("Johnny Appleseed"), Some(ParticipantOrganization("Riverside")), isPresiding = true)
      val judge4 = Judge(ParticipantName("Owlfred"), Some(ParticipantOrganization("OpenStudy")), isPresiding = true)

      policyUnderTest.isValid(matchedTeams, judge1) should equal(true)
      policyUnderTest.isValid(matchedTeams, judge2) should equal(false)
      policyUnderTest.isValid(matchedTeams, judge3) should equal(false)
      policyUnderTest.isValid(matchedTeams, judge4) should equal(true)
    }

    "only permit scoring judges without organization or from different organizations" in {
      val team1 = CompetingTeam(ParticipantName("Riverside 1"), ParticipantOrganization("Riverside"))
      val team2 = CompetingTeam(ParticipantName("Eastside 1"), ParticipantOrganization("Eastside"))
      val presidingJudge = Judge(ParticipantName("Bob Jones"), None, isPresiding = true)
      val presidingJudge2 = Judge(ParticipantName("Will Smartt"), Some(ParticipantOrganization("OpenStudy")))

      val matchedTeamsWithPresiding = MatchedTeamsWithPresidingJudge(team1, team2, presidingJudge)
      val matchedTeamsWithPresiding2 = MatchedTeamsWithPresidingJudge(team1, team2, presidingJudge2)

      val judge1 = Judge(ParticipantName("Bob Jones"), None, isScoring = true)
      val judge2 = Judge(ParticipantName("Mark Appleseed"), Some(ParticipantOrganization("Eastside")), isScoring = true)
      val judge3 = Judge(ParticipantName("Johnny Appleseed"), Some(ParticipantOrganization("Riverside")), isScoring = true)
      val judge4 = Judge(ParticipantName("Owlfred"), Some(ParticipantOrganization("OpenStudy")), isScoring = true)

      policyUnderTest.isValid(matchedTeamsWithPresiding, judge1) should equal(true)
      policyUnderTest.isValid(matchedTeamsWithPresiding, judge2) should equal(false)
      policyUnderTest.isValid(matchedTeamsWithPresiding, judge3) should equal(false)
      policyUnderTest.isValid(matchedTeamsWithPresiding, judge4) should equal(true)
      policyUnderTest.isValid(matchedTeamsWithPresiding2, judge4) should equal(false)
    }
  }

  "NotAPreviousPolicy" should {
    val policyUnderTest = NotAPreviousPolicy

    val team1Uuid = UUID.randomUUID()
    val team2Uuid = UUID.randomUUID()
    val presidingJudgeUuid = UUID.randomUUID()
    val scoringJudgeUuid = UUID.randomUUID()

    val historicalMatch = HistoricalTrial(
      team1Uuid,
      Seq(10, 10),
      team2Uuid,
      Seq(20, 20),
      presidingJudgeUuid,
      scoringJudgeUuid
    )

    val team1 = CompetingTeam(
      ParticipantName("Riverside 1"),
      ParticipantOrganization("Riverside"),
      matchHistory = Seq(historicalMatch),
      id = team1Uuid
    )
    val team2 = CompetingTeam(
      ParticipantName("Eastside 1"),
      ParticipantOrganization("Eastside"),
      matchHistory = Seq(historicalMatch),
      id = team2Uuid
    )
    val team3 = CompetingTeam(
      ParticipantName("Eastside 2"),
      ParticipantOrganization("Eastside")
    )

    val judge1 = Judge(
      ParticipantName("Bob Jones"),
      None,
      id = presidingJudgeUuid,
      matchHistory = Seq(historicalMatch),
      isPresiding = true
    )
    val judge2 = Judge(
      ParticipantName("W.L. Weller"),
      None,
      isPresiding = true
    )

    val scoring1 = Judge(
      ParticipantName("Dr. Frankenstein"),
      None,
      id = scoringJudgeUuid,
      matchHistory = Seq(historicalMatch),
      isScoring = true
    )
    val scoring2 = Judge(
      ParticipantName("Mr. Kind"),
      None,
      isScoring = true
    )

    "only permit other teams that haven't played this team" in {
      policyUnderTest.isValid(MatchSeed(team1), team2) should equal(false)
      policyUnderTest.isValid(MatchSeed(team2), team1) should equal(false)
      policyUnderTest.isValid(MatchSeed(team1), team3) should equal(true)
      policyUnderTest.isValid(MatchSeed(team2), team3) should equal(true)
      policyUnderTest.isValid(MatchSeed(team3), team1) should equal(true)
      policyUnderTest.isValid(MatchSeed(team3), team2) should equal(true)
    }

    "only permit presiding judges that haven't presided over either team" in {
      policyUnderTest.isValid(MatchedTeams(team1, team3), judge1) should equal(false)
      policyUnderTest.isValid(MatchedTeams(team3, team2), judge1) should equal(false)
      policyUnderTest.isValid(MatchedTeams(team1, team3), judge2) should equal(true)
      policyUnderTest.isValid(MatchedTeams(team3, team2), judge2) should equal(true)
    }

    "only permit scoring judges that haven't scored over either team" in {
      policyUnderTest.isValid(MatchedTeamsWithPresidingJudge(team1, team3, judge2), scoring1) should equal(false)
      policyUnderTest.isValid(MatchedTeamsWithPresidingJudge(team3, team1, judge2), scoring1) should equal(false)
      policyUnderTest.isValid(MatchedTeamsWithPresidingJudge(team1, team3, judge2), scoring2) should equal(true)
      policyUnderTest.isValid(MatchedTeamsWithPresidingJudge(team3, team1, judge2), scoring2) should equal(true)
    }

    "only permit scoring judges that haven't paired with prior presiding judges" in {
      policyUnderTest.isValid(MatchedTeamsWithPresidingJudge(team1, team3, judge1), scoring1) should equal(false)
    }
  }

  "AndMatchingPolicy" should {
    "only permit matches satisfying all component policies" in {
      val randomTeam1: CompetingTeam = teamGen.apply(Gen.Parameters.default, Seed.random()).get
      val randomTeam2: CompetingTeam = teamGen.apply(Gen.Parameters.default, Seed.random()).get

      val alwaysTruePolicy = new MatchingPolicy {
        def isValid(partialMatch: PartialRoundMatch, proposedParticipant: Participant) = true
      }
      val alwaysFalsePolicy = new MatchingPolicy {
        def isValid(partialMatch: PartialRoundMatch, proposedParticipant: Participant) = false
      }

      AndMatchingPolicy(alwaysTruePolicy, alwaysTruePolicy).isValid(MatchSeed(randomTeam1), randomTeam1) should equal (true)
      AndMatchingPolicy(alwaysTruePolicy, alwaysFalsePolicy).isValid(MatchSeed(randomTeam1), randomTeam1) should equal (false)
      AndMatchingPolicy(alwaysFalsePolicy, alwaysFalsePolicy).isValid(MatchSeed(randomTeam1), randomTeam1) should equal (false)
    }
  }
}
