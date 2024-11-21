package main.java.ryans.blog.model;

public class Post{
    private int id;
    private String title;
    private String description;
    private String content;
    private int userId;
    private String createdAt;


    public Post(int id, String title, String description, String content, int userId, String createdAt){
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.userId=userId;
        this.createdAt = createdAt;
    }


    private int getId(){
        return this.id;
    }
    private void setId(int id){
        this.id = id;
    }
    private String getTitle(){
        return this.title;
    }
    private void setTitle(String title){
        this.title = title;
    }
    private String getDescription(){
        return description;
    }
    private String setDescription(String description){
        this.description = description;
    }
    private String getContent(){
        return content;
    }
    private String
}
