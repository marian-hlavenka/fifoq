package com.hlavenka.repository;

import java.util.List;

import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.hlavenka.entity.User;

/**
 * Repository class for CRUD operations on {@code User} entity
 */
public class UserRepository {

    private SessionFactory sessionFactory;

    public UserRepository() {
        Configuration config = new Configuration().configure();
        config.addAnnotatedClass(User.class);
        sessionFactory = config.buildSessionFactory();
    }

    /**
     * Inserts the user to database
     * @param user - user to be added
     * @see User
     */
    public void addUser(User user) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(user);
        session.getTransaction().commit();
        session.close();
    }

    /**
     * Retrieves all users from database
     * @see User
     */
    public List<User> getAllUsers() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        TypedQuery<User> query = session.createQuery("FROM User", User.class);
        List<User> users = query.getResultList();
        session.close();
        return users;
    }

    /**
     * Deletes all users from database
     * @see User
     */
    public void deleteAllUsers() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.createQuery("DELETE FROM User").executeUpdate();
        session.getTransaction().commit();
        session.close();
    }
}
