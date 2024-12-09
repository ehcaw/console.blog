package ryans;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import org.junit.Test;
import ryans.DbTest;
import ryans.blog.dao.CommentDAO;
import ryans.blog.dao.PostDAO;
import ryans.blog.dao.UserDAO;
import ryans.blog.model.Comment;
import ryans.blog.model.Post;
import ryans.blog.model.User;

public class CommentDAOTest extends DbTest {

    private CommentDAO commentDao;
    private PostDAO postDao;
    private UserDAO userDao;
    private int testUserId;
    private int testPostId;

    @Override
    public void setUp() throws SQLException {
        try {
            super.setUp();
            commentDao = new CommentDAO(connection);
            postDao = new PostDAO(connection);
            userDao = new UserDAO(connection);

            // Create test user with unique username
            User testUser = new User();
            testUser.setUsername("commenter" + System.currentTimeMillis());
            testUser.setPassword("pass123");
            User created = userDao.create(testUser);
            testUserId = created.getUserId();

            // Create test post
            Post testPost = new Post();
            testPost.setTitle("Test Post");
            testPost.setContent("Test Content");
            testPost.setDescription("Test Description"); // Add description if required
            testPost.setUserId(testUserId);
            Post createdPost = postDao.create(testPost);
            testPostId = createdPost.getId();
        } catch (Exception e) {
            throw new SQLException("Failed to set up CommentDAOTest", e);
        }
    }

    @Test
    public void testCreateComment() throws Exception {
        // Given
        Comment comment = new Comment();
        comment.setContent("Test Comment");
        comment.setPostId(testPostId);
        comment.setUserId(testUserId);

        // When
        Comment created = commentDao.create(comment);

        // Then
        assertNotNull("Created comment should not be null", created);
        assertNotNull("Created comment should have an ID", created.getId());
        assertEquals(
            "Content should match",
            "Test Comment",
            created.getContent()
        );
    }

    @Test
    public void testFindByPostId() throws Exception {
        // Given
        Comment comment = new Comment();
        comment.setContent("Find this comment");
        comment.setPostId(testPostId);
        comment.setUserId(testUserId);
        commentDao.create(comment);

        // When
        List<Comment> comments = commentDao.findByPostId(testPostId);

        // Then
        assertFalse("Should find comments for post", comments.isEmpty());
        assertEquals(
            "Should find the test comment",
            "Find this comment",
            comments.get(0).getContent()
        );
    }
}
