package ryans;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import ryans.blog.app.Database;

public class DbTest {

    protected Connection connection;

    @Before
    public void setUp() throws SQLException {
        connection = Database.getConnection();
        connection.setAutoCommit(false); // Start transaction for each test
    }

    @After
    public void tearDown() throws SQLException {
        if (connection != null) {
            try {
                connection.rollback(); // Rollback changes after each test
            } finally {
                connection.close();
            }
        }
    }
}
