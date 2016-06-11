package services;

import model.DAO.Impl.UserDAOImpl;
import model.Entities.User;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class RegisterNewUser {

    private static Logger logger = Logger.getLogger(RegisterNewUser.class);

    public static boolean registerNewUser(String firstName, String lastName, String email, String password, SessionFactory sessionFactory) {
        boolean result = false;
        logger.info(String.format("Trying to register new user:: %s , %s, %s", firstName, lastName, email));
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            User user = new User(firstName, lastName, email, password);
            UserDAOImpl userDAO = new UserDAOImpl(User.class, session);
            userDAO.create(user);
            transaction.commit();
            result = true;
            logger.info("New user registered.");
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }
}
