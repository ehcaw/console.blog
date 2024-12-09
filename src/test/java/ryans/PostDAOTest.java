package ryans;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import org.junit.Test;
import ryans.DbTest;
import ryans.blog.dao.PostDAO;
import ryans.blog.dao.UserDAO;
import ryans.blog.model.Post;
import ryans.blog.model.User;

public class PostDAOTest extends DbTest {

    private PostDAO postDao;
    private UserDAO userDao;
    private int testUserId;

    @Override
    public void setUp() throws SQLException {
        try {
            super.setUp();
            postDao = new PostDAO(connection);
            userDao = new UserDAO(connection);

            // Create a test user for posts
            User testUser = new User();
            testUser.setUsername("postwriter" + System.currentTimeMillis()); // Make username unique
            testUser.setPassword("pass123");
            User created = userDao.create(testUser);
            testUserId = created.getUserId();
        } catch (Exception e) {
            throw new SQLException("Failed to set up PostDAOTest", e);
        }
    }

    @Test
    public void testCreatePost() throws Exception {
        // Given
        Post post = new Post();
        post.setTitle("Test Post");
        post.setContent("Test Content");
        post.setDescription("Test Description");
        post.setUserId(testUserId);

        // When
        Post createdPost = postDao.create(post);

        // Then
        assertNotNull("Created post should not be null", createdPost);
        assertNotNull("Created post should have an ID", createdPost.getId());
        assertEquals("Title should match", "Test Post", createdPost.getTitle());
    }

    @Test
    public void testFindById() throws Exception {
        // Given
        Post post = new Post();
        post.setTitle("Find Me");
        post.setContent("Find this content");
        post.setDescription("Test Description");
        post.setUserId(testUserId);
        Post created = postDao.create(post);

        // When
        Post found = postDao.findById(created.getId());

        // Then
        assertNotNull("Should find post by ID", found);
        assertEquals("Title should match", "Find Me", found.getTitle());
    }

    @Test
    public void testSearchPosts() throws Exception {
        // Given
        Post post = new Post();
        post.setTitle("Searchable Post");
        post.setContent("Search this content");
        post.setDescription("Test Description");
        post.setUserId(testUserId);
        postDao.create(post);

        // When
        List<Post> found = postDao.searchPosts("Searchable", null, null);

        // Then
        assertFalse("Should find posts matching search", found.isEmpty());
        assertEquals(
            "Should find the test post",
            "Searchable Post",
            found.get(0).getTitle()
        );
    }
}
