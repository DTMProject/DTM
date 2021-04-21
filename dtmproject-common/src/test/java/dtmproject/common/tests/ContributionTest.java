package dtmproject.common.tests;

import java.util.UUID;

import org.junit.Test;

import dtmproject.common.data.ContributionCounter;
import junit.framework.TestCase;

public class ContributionTest extends TestCase {

    private ContributionCounter cc;
    private UUID testPlayerUUID;
    private TestingTeam testingTeam;

    @Override
    protected void setUp() throws Exception {
	this.cc = new ContributionCounter();
	this.testPlayerUUID = UUID.randomUUID();
	this.testingTeam = new TestingTeam();
    }

    @Test
    public void testPlayerTimesInTeams() {
	cc.playerJoined(testPlayerUUID, testingTeam);
	cc.playerLeaved(testPlayerUUID, testingTeam);

	cc.playerJoined(testPlayerUUID, testingTeam);
	cc.playerLeaved(testPlayerUUID, testingTeam);
    }

}
