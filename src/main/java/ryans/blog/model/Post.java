package ryans.blog.model;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private Integer id;
    private Integer userId;
    private String title;
    private String description;
    private String content;
    private String createdAt;
    private List<String> tags;

    public Post() {
        this.tags = new ArrayList<>();
    }

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
        this.tags = new ArrayList<>();
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

    public List<String> getTags() {
        return tags != null ? tags : new ArrayList<>();
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
