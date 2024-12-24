package beans;


import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import hibernateHelpers.HibernateSessionFactory;
import lombok.Getter;

@ManagedBean(name = "dataBase", eager = true)
@ApplicationScoped
@Getter
public class DataBaseBean {
    private SessionFactory sessionFactory;
    private List<HitResult> cachedPoints;

    @PostConstruct
    public void init() {
        try {
            sessionFactory = HibernateSessionFactory.getSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Инициализация SessionFactory завершилась неудачей: " + ex);
            throw new RuntimeException(ex);
        }
    }

    public String addPointToDatabase(HitResult hitResult) {
        cachedPoints.add(hitResult);

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(hitResult);
            transaction.commit();
            return "added " + hitResult;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "error";
        }
    }

    public void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public List<HitResult> cachePointsFromDatabase() {
        try (Session session = sessionFactory.openSession()) {
            cachedPoints = session.createQuery("FROM HitResult", HitResult.class).getResultList();
        }

        return cachedPoints;
    }
}
