package ryans.blog.model;

public class Comment {

    private int id;
    private String content;
    private int postId;
    private int userId;
    private String createdAt;

    // Constructors
    public Comment() {}

    public Comment(int id, String content, int postId, int userId) {
        this.id = id;
        this.content = content;
        this.postId = postId;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Optional: Override toString() method for easier debugging
    @Override
    public String toString() {
        return (
            "Comment{" +
            "id=" +
            id +
            ", content='" +
            content +
            '\'' +
            ", postId=" +
            postId +
            ", userId=" +
            userId +
            ", createdAt='" +
            createdAt +
            '\'' +
            '}'
        );
    }
}
