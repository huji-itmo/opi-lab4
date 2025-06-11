package interfaces;

import java.io.Serializable;
import java.util.List;

import org.hibernate.SessionFactory;

import beans.HitResult;

public interface IDataBaseMBean extends Serializable {

    SessionFactory getSessionFactory();

    List<HitResult> getCachedPoints();

    void addPointToDatabase(HitResult hitResult);

    void shutdown();

    List<HitResult> cachePointsFromDatabase();

}
