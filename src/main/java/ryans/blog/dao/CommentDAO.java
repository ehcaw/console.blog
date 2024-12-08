package ryans.blog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ryans.blog.model.Comment;

public class CommentDAO {

    private final Connection connection;

    public CommentDAO(Connection connection) {
        this.connection = connection;
    }

    // Create a new comment
    public Comment create(Comment comment) throws SQLException {
        System.out.println("CommentDAO.create - Input comment: " + comment);
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new SQLException("Comment content cannot be null or empty");
        }

        String query =
            "INSERT INTO comments (content, post_id, user_id, created_at) " +
            "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (
            PreparedStatement stmt = connection.prepareStatement(
                query,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            System.out.println("Setting prepared statement parameters:");
            System.out.println("1. Content: " + comment.getContent());
            System.out.println("2. Post ID: " + comment.getPostId());
            System.out.println("3. User ID: " + comment.getUserId());

            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getPostId());
            stmt.setInt(3, comment.getUserId());

            int affectedRows = stmt.executeUpdate();
            System.out.println("Affected rows: " + affectedRows);

            if (affectedRows == 0) {
                throw new SQLException(
                    "Creating comment failed, no rows affected."
                );
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    System.out.println("Generated comment ID: " + newId);
                    comment.setId(newId);
                    Comment created = findById(comment.getId());
                    System.out.println("Retrieved created comment: " + created);
                    return created;
                } else {
                    throw new SQLException(
                        "Creating comment failed, no ID obtained."
                    );
                }
            }
        }
    }

    // Find comment by ID
    public Comment findById(int id) throws SQLException {
        String query = "SELECT * FROM comments WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToComment(rs);
                }
            }
        }
        return null;
    }

    // Get all comments for a specific post
    public List<Comment> findByPostId(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query =
            "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        }
        return comments;
    }

    // Get all comments by a specific user
    public List<Comment> findByUserId(int userId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query =
            "SELECT * FROM comments WHERE user_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        }
        return comments;
    }

    // Update a comment
    public boolean update(Comment comment) throws SQLException {
        String query =
            "UPDATE comments SET content = ? WHERE id = ? AND user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getId());
            stmt.setInt(3, comment.getUserId());

            return stmt.executeUpdate() > 0;
        }
    }

    // Delete a comment
    public boolean delete(int commentId, int userId) throws SQLException {
        String query = "DELETE FROM comments WHERE id = ? AND user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, commentId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    // Delete all comments for a specific post
    public void deleteByPostId(int postId) throws SQLException {
        String query = "DELETE FROM comments WHERE post_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            stmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet to Comment object
    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getInt("id"));
        comment.setContent(rs.getString("content"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setUserId(rs.getInt("user_id"));
        comment.setCreatedAt(rs.getString("created_at"));
        return comment;
    }

    // Get comment count for a post
    public int getCommentCount(int postId) throws SQLException {
        String query =
            "SELECT COUNT(*) as count FROM comments WHERE post_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
                return 0;
            }
        }
    }
}
