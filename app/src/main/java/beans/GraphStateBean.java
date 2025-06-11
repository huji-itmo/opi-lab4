package beans;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import exceptions.BadParameterException;
import exceptions.MissingParametersException;
import interfaces.IDataBaseMBean;
import interfaces.IGraphStateMBean;
import interfaces.IHitPercentMBean;
import interfaces.IHitStatsMBean;
import lombok.Data;

@ManagedBean(name = "graphStateBean", eager = true)
@SessionScoped
@Data
public class GraphStateBean implements IGraphStateMBean {
    // private DataEncoder encoder = new JsonEncoder();

    private Long XToSend = 0L;
    private Double YToSend = 0D;
    private Long RToSend = 1L;

    private Boolean cachedHit = null;
    private String cachedServerTime = null;
    private String cachedDurationMilliSeconds = null;

    public List<Consumer<HitResult>> onNewHitResult = new ArrayList<Consumer<HitResult>>();

    @Inject
    private IDataBaseMBean dataBaseBean;

    @Inject
    private IHitStatsMBean hitStatsBean;

    @Inject
    private IHitPercentMBean hitPercentBean;

    @Override
    public String getIsNoobMessage() {
        return hitStatsBean.getIsNoobMessage();
    }

    @Override
    public float getPercent() {
        return hitPercentBean.getPercent(hitStatsBean);
    }

    @PostConstruct
    public void init() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.example.beans:type=graphStateBean,name=graphStateBean");
            StandardMBean mbean = new StandardMBean(this, IGraphStateMBean.class);
            mbs.registerMBean(mbean, name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataBaseBean.cachePointsFromDatabase();
    }

    @Override
    public List<HitResult> getCachedPoints() {
        return dataBaseBean.getCachedPoints();
    }

    @Override
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

    @Override
    public boolean isFormValid() {
        return XToSend != null && YToSend != null && RToSend != null;
    }
}
