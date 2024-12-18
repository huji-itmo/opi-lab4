package hz_kak_nazvat;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import beans.HitResult;

public class HibernateSessionFactory {

    public static final String USER_ENV_NAME = "POSTGRES_USER";
    public static final String PASSWORD_ENV_NAME = "POSTGRES_PASSWORD";

    static Configuration config = null;

    public static SessionFactory getSessionFactory() throws IOException {
        if (config == null) {
            Properties customProperties = new Properties(2);

            String postgresUser = Objects.requireNonNull(System.getenv(USER_ENV_NAME), "Not found env variable " + USER_ENV_NAME);
            String postgresPassword = Objects.requireNonNull(System.getenv(PASSWORD_ENV_NAME), "Not found env variable " + PASSWORD_ENV_NAME);

            customProperties.setProperty("hibernate.connection.username", postgresUser);
            customProperties.setProperty("hibernate.connection.password", postgresPassword);

            config = new Configuration()
                    .configure()
                    .addAnnotatedClass(HitResult.class)
                    .addProperties(customProperties);
        }


        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(config.getProperties());

        return config.buildSessionFactory(builder.build());
    }
}
