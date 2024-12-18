package beans;


import java.util.concurrent.LinkedBlockingDeque;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import hz_kak_nazvat.HibernateSessionFactory;

@ManagedBean(name = "dataBase", eager = true)
@SessionScoped
public class DataBaseBean {
    private String result;
    private SessionFactory sessionFactory;
    private LinkedBlockingDeque<HitResult> points;

    public DataBaseBean() {
        try {
            sessionFactory = HibernateSessionFactory.getSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Инициализация SessionFactory завершилась неудачей: " + ex);
            throw new RuntimeException(ex);
        }
    }


    public String addPoint(HitResult hitResult) {
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


    public LinkedBlockingDeque<HitResult> getPoints() {
        return points;
    }

    public void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public String getAllPoints() {
        try (Session session = sessionFactory.openSession()) {
            points = new LinkedBlockingDeque<>(session.createQuery("FROM HitResult", HitResult.class).getResultList());
            result = "goToTablePage";
            return result;
        }
    }
}
