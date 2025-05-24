package beans;

import java.io.Serializable;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

@ManagedBean(name = "hitPercentBean", eager = true)
@ApplicationScoped
public class HitPercentBean implements Serializable {
    public float getPercent(HitStatsBean hitStatsBean) {
        if (hitStatsBean.getTotalHits() == 0) {
            return 0;
        }

        return (float) hitStatsBean.getTotalHits() / (float) hitStatsBean.getTotalPoints();
    }

}
