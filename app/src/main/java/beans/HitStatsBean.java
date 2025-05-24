package beans;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

@ManagedBean(name = "graphStateBean", eager = true)
@ApplicationScoped
public class HitStatsBean {
    @Inject
    GraphStateBean graphStateBean;

    private static int totalPoints = 0;
    private static int totalHits = 0;

    @PostConstruct
    public void init() {
        graphStateBean.onNewHitResult.add(HitStatsBean::onNewHitResult);

    }

    public static void onNewHitResult(HitResult res) {
        totalPoints++;
        if (res.getHit()) {
            totalHits++;
        }

        System.out.println("totalPoints=" + Integer.toString(totalPoints));
        System.out.println("totalHits=" + Integer.toString(totalHits));
    }
}
