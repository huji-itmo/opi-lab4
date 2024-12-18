package beans;


import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;

import encoders.DataEncoder;
import encoders.JsonEncoder;
import exceptions.BadParameterException;
import exceptions.MissingParametersException;
import lombok.Data;

@ManagedBean
@SessionScoped
@Data
public class GraphStateBean {
    private DataEncoder encoder = new JsonEncoder();

    private Double xToSend;
    private Double yToSend;
    private Integer rToSend;
    private Deque<HitResult> cachedPoints = new LinkedBlockingDeque<>();
    private String pointsJsonCache = "";
    @Inject
    private DataBaseBean dataBaseBean;

    @PostConstruct
    public void init() {
        dataBaseBean.getAllPoints();
        cachedPoints = dataBaseBean.getPoints();
        pointsJsonCache = encoder.getEncodedHitTable("application/json", cachedPoints.stream());
        rToSend = 1;
    }

    public Deque<HitResult> getCachedPoints() {
        return cachedPoints;
    }

    public String getPointsJsonCache() {
        return pointsJsonCache;
    }

    public void setPointsJsonCache(String pointsJson) {
        this.pointsJsonCache = pointsJson;
    }

    public void addPointToDatabase() {
        if (isFormValid()) {
            HitResult newPoint;
            try {
                newPoint = HitResult.createNewHitData(new RequestData(xToSend, yToSend, rToSend), System.nanoTime());
                cachedPoints.add(newPoint);
                dataBaseBean.addPoint(newPoint);
                pointsJsonCache = encoder.getEncodedHitTable("application/json", cachedPoints.stream());
            } catch (MissingParametersException | BadParameterException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public boolean isFormValid() {
        return xToSend != null && yToSend != null && rToSend != null;
    }
}
