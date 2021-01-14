package dtmproject.scoreboard;

/**
 * Class for generating unique spacers for scoreboards. This class solves the
 * problem of having multiple entries with the same name without changing the
 * scoreboard visually.
 * 
 * @author Juubes
 */
public class ScoreboardSpacerHandler {
	private int spacerInt = 1;

	public String getSpacer(int count) {
		String val = "";
		for (int i = 0; i < count; i++) {
			val += " ";
		}
		return val;
	}

	public String getUnusedSpacer() {
		String ready = "";
		spacerInt++;
		for (int i = 0; i < spacerInt % 30; i++)
			ready += " ";

		return ready;
	}

}
