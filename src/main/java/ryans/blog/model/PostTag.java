package ryans.blog.model;

public class PostTag {

    Integer postId;
    Integer tagId;

    public PostTag(Integer postId, Integer tagId) {
        this.postId = postId;
        this.tagId = tagId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public Integer getTagId(int tagId) {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }
}
