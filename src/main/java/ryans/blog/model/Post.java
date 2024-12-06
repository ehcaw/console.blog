package ryans.blog.model;

public class Post {

    private Integer id;
    private String title;
    private String description;
    private String content;
    private Integer userId;
    private String createdAt;

    public Post() {}

    public Post(
        Integer id,
        String title,
        String description,
        String content,
        int userId,
        String createdAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }
}
