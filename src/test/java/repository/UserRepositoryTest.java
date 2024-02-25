package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

import com.hlavenka.entity.User;
import com.hlavenka.repository.UserRepository;

class UserRepositoryTest {

    private static SessionFactory testSessionFactory;
    private Session session;
    private UserRepository userRepository;

    @BeforeAll
    static void setup() {
        Configuration config = new Configuration().configure("hibernate-test.cfg.xml");
        config.addAnnotatedClass(User.class);
        testSessionFactory = config.buildSessionFactory();
    }

    @BeforeEach
    void setupThis() throws Exception {
        userRepository = new UserRepository();
        Field sessionFactoryField = ReflectionUtils.findFields(UserRepository.class, f -> f.getName().equals("sessionFactory"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).get(0);
        sessionFactoryField.setAccessible(true);
        sessionFactoryField.set(userRepository, testSessionFactory);

        // open session
        session = testSessionFactory.openSession();
    }

    @AfterEach
    void tearThis() throws Exception {
        // delete data from table
        session.beginTransaction();
        session.createQuery("DELETE FROM User").executeUpdate();
        session.getTransaction().commit();
        // close session
        session.close();
    }

    @AfterAll
    static void tear() {
        testSessionFactory.close();
    }

    @Test
    void addUser_successful() {
        User user = new User(1, "guid1", "John");
        userRepository.addUser(user);
        List<User> users = session.createQuery("FROM User", User.class).getResultList();

        assertEquals(1, users.size());
        assertEquals(user, users.get(0));
    }

    @Test
    void getAllUsers_successful() {
        List<User> expectedUsers = createListOfUsers();
        session.beginTransaction();
        createListOfUsers().forEach(session::save);
        session.getTransaction().commit();


        List<User> users = userRepository.getAllUsers();

        assertEquals(expectedUsers.size(), users.size());
        assertTrue(expectedUsers.containsAll(users));
        assertTrue(users.containsAll(expectedUsers));
    }

    @Test
    void deleteAllUsers_successful() {
        session.beginTransaction();
        createListOfUsers().forEach(session::save);
        session.getTransaction().commit();

        List<User> usersBeforeDelete = session.createQuery("FROM User", User.class).getResultList();

        assertEquals(2, usersBeforeDelete.size());

        userRepository.deleteAllUsers();
        List<User> usersAfterDelete = session.createQuery("FROM User", User.class).getResultList();
        assertEquals(0, usersAfterDelete.size());
    }

    private List<User> createListOfUsers() {
        User user1 = new User(1, "guid1", "John");
        User user2 = new User(2, "guid2", "Mike");
        return List.of(user1, user2);
    }
}
