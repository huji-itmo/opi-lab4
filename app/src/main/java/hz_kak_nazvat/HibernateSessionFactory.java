package hz_kak_nazvat;

import java.io.IOException;
import java.util.Objects;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import beans.HitResult;

public class HibernateSessionFactory {

    public static final String USER_ENV_NAME = "POSTGRES_USER";
    public static final String PASSWORD_ENV_NAME = "POSTGRES_PASSWORD";

    static Configuration config = null;

    public static SessionFactory getSessionFactory() throws IOException {
        if (config == null) {
            String postgresUser = Objects.requireNonNull(System.getenv(USER_ENV_NAME), "Not found env variable " + USER_ENV_NAME);
            String postgresPassword = Objects.requireNonNull(System.getenv(PASSWORD_ENV_NAME), "Not found env variable " + PASSWORD_ENV_NAME);

            config = new Configuration()
                .configure("hibernate.cfg.xml")
                .setProperty("hibernate.connection.username", postgresUser)
                .setProperty("hibernate.connection.password", postgresPassword)
                .addAnnotatedClass(HitResult.class);

        }

        return config.buildSessionFactory();
    }
}
