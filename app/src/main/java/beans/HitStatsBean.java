package beans;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;

import lombok.Data;

@ManagedBean(name = "hitStatsBean", eager = true)
@ApplicationScoped
@Data
public class HitStatsBean implements Serializable {

    private int totalPoints = 0;
    private int totalHits = 0;

    private int consecutiveMisses = 0;

    private boolean isNoob = false;

    public boolean getIsNoob() {
        return isNoob;
    };

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

    public String getIsNoobMessage() {
        if (isNoob) {
            return "GIT GUD";
        } else {
            return null;
        }
    }
}
