package beans;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;

import exceptions.BadParameterException;
import exceptions.MissingParametersException;
import lombok.Data;

@ManagedBean(name = "graphStateBean", eager = true)
@SessionScoped
@Data
public class GraphStateBean {
    // private DataEncoder encoder = new JsonEncoder();

    private Long XToSend = 0L;
    private Double YToSend = 0D;
    private Long RToSend = 1L;

    private Boolean cachedHit = null;
    private String cachedServerTime = null;
    private String cachedDurationMilliSeconds = null;

    public List<Consumer<HitResult>> onNewHitResult = new ArrayList<Consumer<HitResult>>();

    @Inject
    private DataBaseBean dataBaseBean;

    @Inject
    private HitStatsBean hitStatsBean;

    public String getIsNoobMessage() {
        return hitStatsBean.getIsNoobMessage();
    }

    @PostConstruct
    public void init() {
        dataBaseBean.cachePointsFromDatabase();
    }

    public List<HitResult> getCachedPoints() {
        return dataBaseBean.getCachedPoints();
    }

    public void addPointToDatabase() {
        if (isFormValid()) {
            try {
                RequestData requestData = new RequestData(XToSend, YToSend, RToSend);
                requestData.throwIfBadData();

                HitResult newPoint = HitResult.createNewHitData(requestData, System.nanoTime());

                cachedHit = newPoint.getHit();
                cachedServerTime = newPoint.getServerTime();
                cachedDurationMilliSeconds = newPoint.getDurationMilliSeconds();

                dataBaseBean.addPointToDatabase(newPoint);

                hitStatsBean.onNewHitResult(newPoint);
            } catch (MissingParametersException | BadParameterException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public boolean isFormValid() {
        return XToSend != null && YToSend != null && RToSend != null;
    }
}
