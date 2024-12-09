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

    private static final String CREATE_TABLE_SQL =
        """
            CREATE TABLE IF NOT EXISTS posts (
                id SERIAL PRIMARY KEY,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                content TEXT NOT NULL,
                created_at TIMESTAMP NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;

    private static final String INSERT_SQL =
        """
            INSERT INTO posts (user_id, title, description, content, created_at)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id, user_id, title, description, content, created_at
        """;

    private static final String LAST_INSERT_ID =
        """
            SELECT last_insert_rowid() as id
        """;

    private static final String UPDATE_SQL =
        """
            UPDATE posts
            SET title = ?,
                description = ?,
                content = ?
            WHERE id = ?
        """;

    public void createTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        }
    }

    public Post create(Post post) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
            stmt.setInt(1, post.getUserId());
            stmt.setString(2, post.getTitle());
            stmt.setString(3, post.getDescription());
            stmt.setString(4, post.getContent());
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Post createdPost = new Post();
                    createdPost.setId(rs.getInt("id"));
                    createdPost.setUserId(rs.getInt("user_id"));
                    createdPost.setTitle(rs.getString("title"));
                    createdPost.setDescription(rs.getString("description"));
                    createdPost.setContent(rs.getString("content"));
                    createdPost.setCreatedAt(
                        rs.getTimestamp("created_at").toString()
                    );
                    return createdPost;
                }
                throw new SQLException("Creating post failed, no ID obtained.");
            }
        }
    }

    public Post findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM posts WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToPost(rs);
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
                    posts.add(resultSetToPost(rs));
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
                posts.add(resultSetToPost(rs));
            }
        }
        return posts;
    }

    public boolean update(Post post) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getDescription());
            stmt.setString(3, post.getContent());
            stmt.setInt(4, post.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Integer id, Integer userId) throws SQLException {
        // Delete associated tags first
        String deleteTagsSql = "DELETE FROM post_tags WHERE post_id = ?";
        try (
            PreparedStatement pstmt = connection.prepareStatement(deleteTagsSql)
        ) {
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

    public List<Post> searchPosts(
        String title,
        String author,
        List<String> tags
    ) throws SQLException {
        StringBuilder sql = new StringBuilder(
            """
                SELECT DISTINCT p.*
                FROM posts p
                LEFT JOIN users u ON p.user_id = u.id
                LEFT JOIN post_tags pt ON p.id = pt.post_id
                LEFT JOIN tags t ON pt.tag_id = t.id
                WHERE 1=1
            """
        );
        List<Object> params = new ArrayList<>();

        if (title != null && !title.trim().isEmpty()) {
            sql.append(" AND LOWER(p.title) LIKE LOWER(?)");
            params.add("%" + title.trim() + "%");
        }

        if (author != null && !author.trim().isEmpty()) {
            sql.append(" AND LOWER(u.username) LIKE LOWER(?)");
            params.add("%" + author.trim() + "%");
        }

        if (tags != null && !tags.isEmpty()) {
            sql.append(" AND t.name IN (");
            for (int i = 0; i < tags.size(); i++) {
                sql.append(i == 0 ? "?" : ", ?");
                params.add(tags.get(i).trim().toLowerCase());
            }
            sql.append(")");
        }

        sql.append(" ORDER BY p.created_at DESC");

        try (
            PreparedStatement pstmt = connection.prepareStatement(
                sql.toString()
            )
        ) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Post> posts = new ArrayList<>();
                while (rs.next()) {
                    posts.add(resultSetToPost(rs));
                }
                return posts;
            }
        }
    }

    private Post resultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt("id"));
        post.setUserId(rs.getInt("user_id"));
        post.setTitle(rs.getString("title"));
        post.setDescription(rs.getString("description"));
        post.setContent(rs.getString("content"));
        post.setCreatedAt(rs.getTimestamp("created_at").toString());
        post.setTags(new ArrayList<>()); // Tags will be set by TagsDAO
        return post;
    }
}
