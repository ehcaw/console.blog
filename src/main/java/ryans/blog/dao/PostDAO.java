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
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title VARCHAR(200) NOT NULL,
                    description TEXT NOT NULL,
                    content TEXT NOT NULL,
                    user_id INTEGER NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public Post create(Post post) throws SQLException {
        String sql = "INSERT INTO posts (title, description, content, user_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getDescription());
            pstmt.setString(3, post.getContent());
            pstmt.setInt(4, post.getUserId());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    post.setId(rs.getInt(1));
                    return post;
                } else {
                    throw new SQLException("Creating post failed, no ID obtained.");
                }
            }
        }
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
            "SELECT * FROM posts WHERE user_id = ? ORDER BY created_at DESC";
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
        String sql = "SELECT * FROM posts ORDER BY created_at DESC";
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
            "UPDATE posts SET title = ?, description = ?, content = ? WHERE id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getDescription());
            pstmt.setString(3, post.getContent());
            pstmt.setInt(4, post.getId());
            pstmt.setInt(5, post.getUserId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(Integer id, Integer userId) throws SQLException {
        // Delete associated tags first
        String deleteTagsSql = "DELETE FROM post_tags WHERE post_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteTagsSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }

        // Then delete the post
        String sql = "DELETE FROM posts WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
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
        post.setDescription(rs.getString("description"));
        post.setContent(rs.getString("content"));
        post.setUserId(rs.getInt("user_id"));
        return post;
    }
}
