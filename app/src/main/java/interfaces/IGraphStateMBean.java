package interfaces;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

import beans.HitResult;

public interface IGraphStateMBean extends Serializable {

    Long getXToSend();

    Double getYToSend();

    Long getRToSend();

    Boolean getCachedHit();

    String getCachedServerTime();

    String getCachedDurationMilliSeconds();

    List<Consumer<HitResult>> getOnNewHitResult();

    IDataBaseMBean getDataBaseBean();

    IHitStatsMBean getHitStatsBean();

    IHitPercentMBean getHitPercentBean();

    void setXToSend(Long XToSend);

    void setYToSend(Double YToSend);

    void setRToSend(Long RToSend);

    void setCachedHit(Boolean cachedHit);

    void setCachedServerTime(String cachedServerTime);

    void setCachedDurationMilliSeconds(String cachedDurationMilliSeconds);

    void setOnNewHitResult(List<Consumer<HitResult>> onNewHitResult);

    void setDataBaseBean(IDataBaseMBean dataBaseBean);

    void setHitStatsBean(IHitStatsMBean hitStatsBean);

    void setHitPercentBean(IHitPercentMBean hitPercentBean);

    String getIsNoobMessage();

    float getPercent();

    List<HitResult> getCachedPoints();

    void addPointToDatabase();

    boolean isFormValid();

}
