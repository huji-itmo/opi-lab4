package interfaces;

import beans.HitResult;

public interface IHitStatsMBean {

    int getTotalPoints();

    int getTotalHits();

    int getConsecutiveMisses();

    void setTotalPoints(int totalPoints);

    void setTotalHits(int totalHits);

    void setConsecutiveMisses(int consecutiveMisses);

    void setNoob(boolean isNoob);

    boolean getIsNoob();

    void onNewHitResult(HitResult res);

    String getIsNoobMessage();

}
