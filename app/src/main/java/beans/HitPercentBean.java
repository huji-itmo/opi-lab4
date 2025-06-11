package beans;

import java.io.Serializable;
import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import interfaces.IGraphStateMBean;
import interfaces.IHitPercentMBean;
import interfaces.IHitStatsMBean;

@ManagedBean(name = "hitPercentBean", eager = true)
@ApplicationScoped
public class HitPercentBean implements Serializable, IHitPercentMBean {
    @Override
    public float getPercent(IHitStatsMBean hitStatsBean) {
        if (hitStatsBean.getTotalHits() == 0) {
            return 0;
        }

        return (float) hitStatsBean.getTotalHits() / (float) hitStatsBean.getTotalPoints();
    }

    @PostConstruct
    public void init() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.example.beans:type=hitPercentBean,name=hitPercentBean");
            StandardMBean mbean = new StandardMBean(this, IHitPercentMBean.class);
            mbs.registerMBean(mbean, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
