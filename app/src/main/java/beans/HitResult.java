package beans;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import exceptions.BadParameterException;
import exceptions.MissingParametersException;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "HitResults")
public class HitResult implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "x")
    private Long x;
    @Column(name = "y")
    private Double y;
    @Column(name = "r")
    private Long r;
    @Column(name = "hit")
    private Boolean hit;
    @Column(name = "durationMilliSeconds")
    private String durationMilliSeconds;
    @Column(name = "serverTime")
    private String serverTime;

    public String toJsonFields() {
        return String.format(Locale.US, """
                "x": "%d", "y": "%.3f", "r": "%d", "hit":%s, "duration_milliseconds": "%s", "server_time": "%s"
                """, x, y, r, hit ? "true" : "false", durationMilliSeconds, serverTime);
    }

    public String toHTMLTable() {
        return String.format(Locale.US, """
                <tr>
                    <td>%.1f</td>
                    <td>%.3f</td>
                    <td>%d</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                    """, x, y, r, hit ? "true" : "false", durationMilliSeconds, serverTime);
    }

    public HitResult(Long x, Double y, Long r, boolean hit, long durationNanoSeconds) {
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedDateCustom = ZonedDateTime.now().format(customFormatter);

        this.serverTime = formattedDateCustom;
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
        this.durationMilliSeconds = "%.3f ms".formatted((double) durationNanoSeconds / 1000000D);
    }

    public static HitResult createNewHitData(Long x, Double y, Long r, long startTime)
            throws MissingParametersException, BadParameterException {

        RequestData data = new RequestData(x, y, r);
        data.throwIfBadData();
        boolean hitResult = CoordinateSpace.testHit(data);
        long durationNanoSeconds = System.nanoTime() - startTime;
        HitResult hitData = new HitResult(data.getX(), data.getY(), data.getR(), hitResult, durationNanoSeconds);

        return hitData;
    }

    public static HitResult createNewHitData(RequestData data, long startTime)
            throws MissingParametersException, BadParameterException {
        data.throwIfBadData();
        boolean hitResult = CoordinateSpace.testHit(data);
        long durationNanoSeconds = System.nanoTime() - startTime;
        HitResult hitData = new HitResult(data.getX(), data.getY(), data.getR(), hitResult, durationNanoSeconds);

        return hitData;
    }
}
