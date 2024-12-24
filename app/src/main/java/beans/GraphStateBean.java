package beans;


import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.PostConstruct;
import javax.faces.bean.*;

import javax.inject.Inject;

import encoders.DataEncoder;
import encoders.JsonEncoder;
import exceptions.BadParameterException;
import exceptions.MissingParametersException;
import lombok.Data;

@ManagedBean(name="graphStateBean", eager = true)
@SessionScoped
@Data
public class GraphStateBean {
    private DataEncoder encoder = new JsonEncoder();

    private Long XToSend = 0L;
    private Double YToSend = 0D;
    private Long RToSend = 0L;

    private Boolean cachedHit = null;
    private String cachedServerTime = null;
    private String cachedDurationMilliSeconds = null;

    private String pointsJsonCache = "";
    @Inject
    private DataBaseBean dataBaseBean;

    @PostConstruct
    public void init() {
        dataBaseBean.getAllPoints();
        pointsJsonCache = encoder.getEncodedHitTable("application/json", getCachedPoints().stream());
        RToSend = 1L;
    }

    public List<HitResult> getCachedPoints() {
        return dataBaseBean.getPoints();
    }

    public void addPointToDatabase() {
        if (isFormValid()) {
            HitResult newPoint;
            try {
                newPoint = HitResult.createNewHitData(new RequestData(XToSend, YToSend, RToSend), System.nanoTime());
                cachedHit = newPoint.getHit();
                cachedServerTime = newPoint.getServerTime();
                cachedDurationMilliSeconds = newPoint.getDurationMilliSeconds();

                getCachedPoints().add(newPoint);
                dataBaseBean.addPoint(newPoint);
                pointsJsonCache = encoder.getEncodedHitTable("application/json", getCachedPoints().stream());
            } catch (MissingParametersException | BadParameterException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public boolean isFormValid() {
        return XToSend != null && YToSend != null && RToSend != null;
    }
}
