package ryans;

import static org.junit.Assert.*;

import org.junit.Test;
import ryans.DbTest;
import ryans.blog.dao.UserDAO;
import ryans.blog.model.User;

public class UserDAOTest extends DbTest {

    private UserDAO userDao;

    @Override
    public void setUp() {
        try {
            super.setUp();
            userDao = new UserDAO(connection);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
    }

    @Test
    public void testCreateUser() throws Exception {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpass");

        // When
        User createdUser = userDao.create(user);

        // Then
        assertNotNull("Created user should not be null", createdUser);
        assertNotNull(
            "Created user should have an ID",
            createdUser.getUserId()
        );
        assertEquals(
            "Username should match",
            "testuser",
            createdUser.getUsername()
        );
    }

    @Test
    public void testFindByUsername() throws Exception {
        // Given
        User user = new User();
        user.setUsername("findme");
        user.setPassword("pass123");
        userDao.create(user);

        // When
        User foundUser = userDao.findByUsername("findme");

        // Then
        assertNotNull("Should find user by username", foundUser);
        assertEquals(
            "Username should match",
            "findme",
            foundUser.getUsername()
        );
    }

    @Test
    public void testFindByUsernameAndPassword() throws Exception {
        // Given
        User user = new User();
        user.setUsername("logintest");
        user.setPassword("pass123");
        userDao.create(user);

        // When
        User foundUser = userDao.findByUsernameAndPassword(
            "logintest",
            "pass123"
        );

        // Then
        assertNotNull("Should find user with correct credentials", foundUser);
        assertEquals(
            "Username should match",
            "logintest",
            foundUser.getUsername()
        );
    }
}
