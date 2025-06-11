package beans;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import hibernateHelpers.HibernateSessionFactory;
import interfaces.IDataBaseMBean;
import lombok.Getter;

@ManagedBean(name = "dataBase", eager = true)
@ApplicationScoped
@Getter
public class DataBaseBean implements IDataBaseMBean {
    private SessionFactory sessionFactory;
    private List<HitResult> cachedPoints;

    @PostConstruct
    public void init() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.example.beans:type=dataBase,name=dataBase");
            StandardMBean mbean = new StandardMBean(this, IDataBaseMBean.class);
            mbs.registerMBean(mbean, name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            sessionFactory = HibernateSessionFactory.getSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Инициализация SessionFactory завершилась неудачей: " + ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void addPointToDatabase(HitResult hitResult) {
        cachedPoints.add(hitResult);

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(hitResult);
            transaction.commit();
            System.out.println("added " + hitResult);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Override
    public List<HitResult> cachePointsFromDatabase() {
        try (Session session = sessionFactory.openSession()) {
            cachedPoints = session.createQuery("FROM HitResult", HitResult.class).getResultList();
        }

        return cachedPoints;
    }
}
