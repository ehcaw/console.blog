package ryans.blog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ryans.blog.model.Tag;

public class TagsDAO {

    private final Connection connection;

    public TagsDAO(Connection connection) {
        this.connection = connection;
    }

    // Create a new tag
    public Tag create(Tag tag) throws SQLException {
        String query =
            "INSERT INTO tags (name, created_at) VALUES (?, CURRENT_TIMESTAMP)";

        try (
            PreparedStatement stmt = connection.prepareStatement(
                query,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            stmt.setString(1, tag.getName().toLowerCase()); // Store tags in lowercase

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                    "Creating tag failed, no rows affected."
                );
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tag.setId(generatedKeys.getInt(1));
                    return findById(tag.getId());
                } else {
                    throw new SQLException(
                        "Creating tag failed, no ID obtained."
                    );
                }
            }
        }
    }

    // Find tag by ID
    public Tag findById(int id) throws SQLException {
        String query = "SELECT * FROM tags WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTag(rs);
                }
            }
        }
        return null;
    }

    // Find tag by name
    public Tag findByName(String name) throws SQLException {
        String query = "SELECT * FROM tags WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTag(rs);
                }
            }
        }
        return null;
    }

    // Get all tags
    public List<Tag> findAll() throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String query = "SELECT * FROM tags ORDER BY name";

        try (
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                tags.add(mapResultSetToTag(rs));
            }
        }
        return tags;
    }

    // Get all tags for a post
    public List<Tag> findByPostId(int postId) throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String query =
            "SELECT t.* FROM tags t " +
            "JOIN post_tags pt ON t.id = pt.tag_id " +
            "WHERE pt.post_id = ? " +
            "ORDER BY t.name";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(mapResultSetToTag(rs));
                }
            }
        }
        return tags;
    }

    // Update a tag
    public boolean update(Tag tag) throws SQLException {
        String query = "UPDATE tags SET name = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tag.getName().toLowerCase());
            stmt.setInt(2, tag.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // Delete a tag
    public boolean delete(int id) throws SQLException {
        // First delete all post-tag relationships
        String deleteRelationsQuery = "DELETE FROM post_tags WHERE tag_id = ?";
        try (
            PreparedStatement stmt = connection.prepareStatement(
                deleteRelationsQuery
            )
        ) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }

        // Then delete the tag
        String deleteTagQuery = "DELETE FROM tags WHERE id = ?";
        try (
            PreparedStatement stmt = connection.prepareStatement(deleteTagQuery)
        ) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // Get popular tags (with post count)
    public List<TagWithCount> getPopularTags(int limit) throws SQLException {
        List<TagWithCount> popularTags = new ArrayList<>();
        String query =
            "SELECT t.*, COUNT(pt.post_id) as post_count " +
            "FROM tags t " +
            "LEFT JOIN post_tags pt ON t.id = pt.tag_id " +
            "GROUP BY t.id " +
            "ORDER BY post_count DESC " +
            "LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Tag tag = mapResultSetToTag(rs);
                    int count = rs.getInt("post_count");
                    popularTags.add(new TagWithCount(tag, count));
                }
            }
        }
        return popularTags;
    }

    // Create if not exists
    public Tag getOrCreate(String tagName) throws SQLException {
        Tag existing = findByName(tagName);
        if (existing != null) {
            return existing;
        }

        Tag newTag = new Tag();
        newTag.setName(tagName);
        return create(newTag);
    }

    // Helper method to map ResultSet to Tag object
    private Tag mapResultSetToTag(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.setId(rs.getInt("id"));
        tag.setName(rs.getString("name"));
        return tag;
    }

    // Inner class for tags with post count
    public static class TagWithCount {

        private final Tag tag;
        private final int count;

        public TagWithCount(Tag tag, int count) {
            this.tag = tag;
            this.count = count;
        }

        public Tag getTag() {
            return tag;
        }

        public int getCount() {
            return count;
        }
    }

    // Search tags by partial name
    public List<Tag> searchByName(String partialName) throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String query = "SELECT * FROM tags WHERE name LIKE ? ORDER BY name";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + partialName.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(mapResultSetToTag(rs));
                }
            }
        }
        return tags;
    }
}
