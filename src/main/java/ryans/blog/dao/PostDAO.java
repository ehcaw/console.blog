package ryans.blog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import ryans.blog.model.PostModel;

public class PostDAO {

    private Connection connection;

    public PostDAO(Connection connection) {
        this.connection = connection;
    }

    public void insertPost(PostModel post) {
        String query =
            "INSERT INTO POSTS (title, description, content, user_id, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getDescription());
            stmt.setString(3, post.getContent()); // Insert formatted content
            stmt.setInt(4, post.getUserId());
            stmt.setString(5, post.getCreatedAt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
