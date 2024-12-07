package ryans.blog.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ryans.blog.model.Post;

public class PostDAO {

    private Connection connection;

    public PostDAO(Connection connection) {
        this.connection = connection;
    }

    public void createTable() throws SQLException {
        String sql =
            """
                CREATE TABLE IF NOT EXISTS posts (
                    post_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title VARCHAR(200) NOT NULL,
                    content TEXT NOT NULL,
                    user_id INTEGER NOT NULL,
                    post_date TIMESTAMP NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public Post create(Post post) throws SQLException {
        String sql =
            "INSERT INTO posts (title, content, user_id) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setLong(3, post.getUserId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException(
                    "Creating post failed, no rows affected."
                );
            }

            // Get the last inserted id
            try (
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")
            ) {
                if (rs.next()) {
                    post.setId(rs.getInt(1));
                }
            }
        }
        return post;
    }

    public Post findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM posts WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPost(rs);
                }
            }
        }
        return null;
    }

    public List<Post> findByAuthor(Integer authorId) throws SQLException {
        String sql =
            "SELECT * FROM posts WHERE user_id = ? ORDER BY post_date DESC";
        List<Post> posts = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, authorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        }
        return posts;
    }

    public List<Post> findAll() throws SQLException {
        String sql = "SELECT * FROM posts ORDER BY post_date DESC";
        List<Post> posts = new ArrayList<>();

        try (
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                posts.add(mapResultSetToPost(rs));
            }
        }
        return posts;
    }

    public boolean update(Post post) throws SQLException {
        String sql =
            "UPDATE posts SET title = ?, content = ? WHERE id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setInt(3, post.getId());
            pstmt.setInt(4, post.getUserId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(Integer postId, Integer userId) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, postId);
            pstmt.setLong(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(Integer postId) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, postId);
            return pstmt.executeUpdate() > 0;
        }
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt("id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setUserId(rs.getInt("user_id"));
        return post;
    }
}
