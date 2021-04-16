package dtmproject.common.data;

import java.text.NumberFormat;
import java.util.UUID;

public interface IDTMSeasonStats {
    public UUID getUUID();

    public int getSeason();

    public int getKills();

    public int getDeaths();

    public int getWins();

    public int getLosses();

    public int getLongestKillStreak();

    public long getPlayTimeWon();

    public long getPlayTimeLost();

    public int getMonumentsDestroyed();

    public default double getKDRatio() {
	// TODO Bad code
	NumberFormat f = NumberFormat.getInstance();
	f.setMaximumFractionDigits(2);
	f.setMinimumFractionDigits(2);

	String KD = f.format((double) getKills() / (double) getDeaths());
	if (getKills() < 1 || getDeaths() < 1)
	    KD = "0.00";
	return Double.parseDouble(KD);
    }

    public int getSum();

    public void increaseKills();

    public void increaseDeaths();

    public void increaseKillStreak();

    public void increaseWins();

    public void increaseLosses();

    public void increasePlayTimeWon(long time);

    public void increasePlayTimeLost(long time);

    public void increaseMonumentsDestroyed();
}
