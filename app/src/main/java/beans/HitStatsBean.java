package beans;

import java.io.Serializable;
import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import interfaces.IHitPercentMBean;
import interfaces.IHitStatsMBean;
import lombok.Data;

@ManagedBean(name = "hitStatsBean", eager = true)
@ApplicationScoped
@Data
public class HitStatsBean implements Serializable, IHitStatsMBean {

    private int totalPoints = 0;
    private int totalHits = 0;

    private int consecutiveMisses = 0;

    private boolean isNoob = false;

    @Override
    public boolean getIsNoob() {
        return isNoob;
    };

    @Override
    public void onNewHitResult(HitResult res) {
        totalPoints++;
        if (res.getHit()) {
            consecutiveMisses = 0;
            totalHits++;
        } else {
            consecutiveMisses++;

            isNoob = consecutiveMisses % 4 == 0;
        }

        System.out.println("totalPoints=" + Integer.toString(totalPoints));
        System.out.println("totalHits=" + Integer.toString(totalHits));
        System.out.println("consecutiveMisses=" + Integer.toString(consecutiveMisses));

    }

    @Override
    public String getIsNoobMessage() {
        if (isNoob) {
            return "GIT GUD";
        } else {
            return null;
        }
    }

    @PostConstruct
    public void init() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.example.beans:type=hitStatsBean,name=hitStatsBean");
            StandardMBean mbean = new StandardMBean(this, IHitStatsMBean.class);
            mbs.registerMBean(mbean, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
