package ryans.blog.app.webapp;

import static spark.Spark.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ryans.blog.app.Database;
import ryans.blog.dao.CommentDAO;
import ryans.blog.dao.PostDAO;
import ryans.blog.dao.TagsDAO;
import ryans.blog.dao.UserDAO;
import ryans.blog.model.Comment;
import ryans.blog.model.Post;
import ryans.blog.model.Tag;
import ryans.blog.model.User;
import spark.Session;

public class BlogController {

    public static void main(String[] args) {
        // Set the port for the web server
        port(9090);

        // Enable session handling
        before((req, res) -> {
            if (req.session(true).isNew()) {
                req.session(true);
            }
        });

        // Authentication middleware for protected routes
        before("/posts/create", (req, res) -> {
            if (req.session().attribute("username") == null) {
                res.redirect("/login");
                halt(401, "Please login first");
            }
        });

        // Login endpoint - GET (show form)
        get("/login", (req, res) -> {
            return "<!DOCTYPE html><html><body>" +
                   "<h1>Login</h1>" +
                   "<form method='post' action='/login'>" +
                   "Username: <input type='text' name='username'><br>" +
                   "Password: <input type='password' name='password'><br>" +
                   "<input type='submit' value='Login'>" +
                   "</form></body></html>";
        });

        // Login endpoint - POST (process form)
        post("/login", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");

            try (Connection conn = Database.getConnection()) {
                UserDAO userDao = new UserDAO(conn);
                User user = userDao.findByUsernameAndPassword(username, password);

                if (user != null) {
                    // Set session attributes
                    req.session(true).attribute("username", username);
                    req.session().attribute("userId", user.getUserId());
                    res.redirect("/posts");
                    return null;
                } else {
                    return "Invalid username or password";
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error during login: " + e.getMessage();
            }
        });

        // Register endpoint - GET (show form)
        get("/register", (req, res) -> {
            return "<!DOCTYPE html><html><body>" +
                   "<h1>Register</h1>" +
                   "<form method='post' action='/register'>" +
                   "Username: <input type='text' name='username'><br>" +
                   "Password: <input type='password' name='password'><br>" +
                   "<input type='submit' value='Register'>" +
                   "</form></body></html>";
        });

        // Register endpoint - POST (process form)
        post("/register", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");

            try (Connection conn = Database.getConnection()) {
                UserDAO userDao = new UserDAO(conn);
                
                // Check if username already exists
                if (userDao.findByUsername(username) != null) {
                    return "Username already exists";
                }

                // Create new user
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
                userDao.create(newUser);
                
                res.redirect("/login");
                return null;
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error during registration: " + e.getMessage();
            }
        });

        // Logout endpoint
        get("/logout", (req, res) -> {
            req.session().removeAttribute("username");
            req.session().removeAttribute("userId");
            res.redirect("/login");
            return null;
        });

        // Create post form
        get("/posts/create", (req, res) -> {
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html><body>");
            sb.append("<h1>Create Post</h1>");
            sb.append("<form action='/posts/create' method='post' style='max-width: 600px; margin: 20px auto;'>");
            sb.append("<div style='margin-bottom: 15px;'>");
            sb.append("<label for='title' style='display: block; margin-bottom: 5px;'>Title:</label>");
            sb.append("<input type='text' id='title' name='title' required style='width: 100%; padding: 8px;'>");
            sb.append("</div>");
            
            sb.append("<div style='margin-bottom: 15px;'>");
            sb.append("<label for='description' style='display: block; margin-bottom: 5px;'>Description:</label>");
            sb.append("<input type='text' id='description' name='description' required style='width: 100%; padding: 8px;'>");
            sb.append("</div>");
            
            sb.append("<div style='margin-bottom: 15px;'>");
            sb.append("<label for='content' style='display: block; margin-bottom: 5px;'>Content:</label>");
            sb.append("<textarea id='content' name='content' required style='width: 100%; height: 200px; padding: 8px;'></textarea>");
            sb.append("</div>");
            
            sb.append("<div style='margin-bottom: 15px;'>");
            sb.append("<label for='tags' style='display: block; margin-bottom: 5px;'>Tags (comma-separated):</label>");
            sb.append("<input type='text' id='tags' name='tags' placeholder='e.g., java, programming, tutorial' style='width: 100%; padding: 8px;'>");
            sb.append("</div>");
            
            sb.append("<button type='submit' style='background: #4CAF50; color: white; padding: 10px 20px; border: none; cursor: pointer;'>Create Post</button>");
            sb.append("</form>");
            sb.append("</body></html>");
            return sb.toString();
        });

        // Create new post
        post("/posts/create", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            if (userId == null) {
                res.redirect("/login");
                return null;
            }

            String title = req.queryParams("title");
            String description = req.queryParams("description");
            String content = req.queryParams("content");
            String tagsInput = req.queryParams("tags");

            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Create post using PostDAO
                    Post newPost = new Post();
                    newPost.setTitle(title);
                    newPost.setDescription(description);
                    newPost.setContent(content);
                    newPost.setUserId(userId);
                    
                    PostDAO postDao = new PostDAO(conn);
                    newPost = postDao.create(newPost);

                    // Process tags if provided
                    if (tagsInput != null && !tagsInput.trim().isEmpty()) {
                        TagsDAO tagsDao = new TagsDAO(conn);
                        String[] tagNames = tagsInput.split(",");
                        
                        for (String tagName : tagNames) {
                            tagName = tagName.trim().toLowerCase();
                            if (!tagName.isEmpty()) {
                                Tag tag = tagsDao.findByName(tagName);
                                if (tag == null) {
                                    tag = new Tag();
                                    tag.setName(tagName);
                                    tag = tagsDao.create(tag);
                                }
                                tagsDao.linkTagToPost(tag.getId(), newPost.getId());
                            }
                        }
                    }

                    conn.commit();
                    res.redirect("/posts");
                    return null;
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error creating post: " + e.getMessage();
            }
        });

        // Route to get all posts
        get("/posts", (req, res) -> {
            res.type("text/html");
            StringBuilder sb = new StringBuilder();
            
            // Add header with login status and navigation
            sb.append("<!DOCTYPE html><html><head><title>Posts</title></head><body>");
            String username = req.session().attribute("username");
            Integer userId = req.session().attribute("userId");
            if (username != null) {
                sb.append("<p>Welcome, ").append(username)
                  .append("! <a href='/logout'>Logout</a></p>");
                sb.append("<p><a href='/posts/create'>Create New Post</a></p>");
            } else {
                sb.append("<p><a href='/login'>Login</a> or <a href='/register'>Register</a></p>");
            }
            
            sb.append("<h1>Posts</h1>");

            try (Connection conn = Database.getConnection()) {
                PostDAO postDao = new PostDAO(conn);
                UserDAO userDao = new UserDAO(conn);
                TagsDAO tagsDao = new TagsDAO(conn);
                CommentDAO commentDao = new CommentDAO(conn);
                
                List<Post> posts = postDao.findAll();
                
                for (Post post : posts) {
                    User author = userDao.findById(post.getUserId());
                    List<Tag> tags = tagsDao.findByPostId(post.getId());
                    List<Comment> comments = commentDao.findByPostId(post.getId());
                    
                    sb.append("<div style='margin-bottom: 20px; padding: 15px; border: 1px solid #ddd; border-radius: 5px;'>")
                      .append("<h2>").append(post.getTitle()).append("</h2>")
                      .append("<p><strong>Author:</strong> ").append(author.getUsername()).append("</p>")
                      .append("<p><strong>Posted:</strong> ").append(post.getCreatedAt()).append("</p>")
                      .append("<p><strong>Description:</strong> ").append(post.getDescription() != null ? post.getDescription() : "").append("</p>")
                      .append("<p>").append(post.getContent()).append("</p>")
                      .append("<p><strong>Tags:</strong> ").append(
                          tags.stream()
                              .map(Tag::getName)
                              .collect(Collectors.joining(", "))
                      ).append("</p>");

                    // Add edit/delete buttons for post owner
                    if (userId != null && userId.equals(post.getUserId())) {
                        sb.append("<div style='margin-top: 10px;'>")
                          .append("<a href='/posts/edit/").append(post.getId())
                          .append("' style='margin-right: 10px;'><button>Edit</button></a>")
                          .append("<form action='/posts/delete/").append(post.getId())
                          .append("' method='post' style='display: inline;'>")
                          .append("<button type='submit'>Delete</button>")
                          .append("</form>")
                          .append("</div>");
                    }

                    // Display comments
                    sb.append("<div style='margin-top: 20px; margin-left: 20px;'>")
                      .append("<h3>Comments</h3>");
                    
                    for (Comment comment : comments) {
                        User commentAuthor = userDao.findById(comment.getUserId());
                        sb.append("<div style='margin-bottom: 10px; padding: 10px; border-left: 3px solid #ddd;'>")
                          .append("<p><strong>").append(commentAuthor.getUsername()).append(":</strong> ")
                          .append(comment.getContent()).append("</p>")
                          .append("</div>");
                    }

                    // Add comment form for logged-in users
                    if (userId != null) {
                        sb.append("<form action='/posts/").append(post.getId()).append("/comment' method='post'>")
                          .append("<textarea name='content' placeholder='Add a comment...' required></textarea><br>")
                          .append("<button type='submit'>Add Comment</button>")
                          .append("</form>");
                    }
                    
                    sb.append("</div></div>");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error fetching posts: " + e.getMessage();
            }

            sb.append("</body></html>");
            return sb.toString();
        });

        // Delete post
        post("/posts/delete/:id", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            if (userId == null) {
                res.redirect("/login");
                return null;
            }

            int postId = Integer.parseInt(req.params(":id"));

            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    PostDAO postDao = new PostDAO(conn);
                    
                    // Check post ownership
                    Post post = postDao.findById(postId);
                    if (post == null || !post.getUserId().equals(userId)) {
                        conn.rollback();
                        return "Unauthorized to delete this post";
                    }
                    
                    // Delete the post - the database will automatically delete related post_tags due to ON DELETE CASCADE
                    if (postDao.delete(postId, userId)) {
                        conn.commit();
                        res.redirect("/posts");
                        return null;
                    } else {
                        conn.rollback();
                        return "Failed to delete post";
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error deleting post: " + e.getMessage();
            }
        });

        // Edit post form
        get("/posts/edit/:id", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            if (userId == null) {
                res.redirect("/login");
                return null;
            }

            int postId = Integer.parseInt(req.params(":id"));

            try (Connection conn = Database.getConnection()) {
                PostDAO postDao = new PostDAO(conn);
                TagsDAO tagsDao = new TagsDAO(conn);
                Post post = postDao.findById(postId);

                if (post == null || !post.getUserId().equals(userId)) {
                    res.redirect("/posts");
                    return null;
                }

                List<Tag> tags = tagsDao.findByPostId(postId);
                String tagString = tags.stream()
                    .map(Tag::getName)
                    .collect(Collectors.joining(", "));

                StringBuilder sb = new StringBuilder();
                sb.append("<!DOCTYPE html><html><head><title>Edit Post</title></head><body>")
                  .append("<h1>Edit Post</h1>")
                  .append("<form action='/posts/edit/").append(postId).append("' method='post'>")
                  .append("<p><label>Title:<br><input type='text' name='title' value='")
                  .append(post.getTitle()).append("' required></label></p>")
                  .append("<p><label>Description:<br><input type='text' name='description' value='")
                  .append(post.getDescription() != null ? post.getDescription() : "").append("'></label></p>")
                  .append("<p><label>Content:<br><textarea name='content' required>")
                  .append(post.getContent()).append("</textarea></label></p>")
                  .append("<p><label>Tags (comma-separated):<br><input type='text' name='tags' value='")
                  .append(tagString).append("'></label></p>")
                  .append("<p><button type='submit'>Update Post</button></p>")
                  .append("</form>")
                  .append("<p><a href='/posts'>Back to Posts</a></p>")
                  .append("</body></html>");

                return sb.toString();
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error loading post: " + e.getMessage();
            }
        });

        // Handle edit post submission
        post("/posts/edit/:id", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            if (userId == null) {
                res.redirect("/login");
                return null;
            }

            int postId = Integer.parseInt(req.params(":id"));
            String title = req.queryParams("title");
            String description = req.queryParams("description");
            String content = req.queryParams("content");
            String tagsInput = req.queryParams("tags");

            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    PostDAO postDao = new PostDAO(conn);
                    Post post = postDao.findById(postId);

                    if (post == null || !post.getUserId().equals(userId)) {
                        res.redirect("/posts");
                        return null;
                    }

                    post.setTitle(title);
                    post.setDescription(description);
                    post.setContent(content);

                    postDao.update(post);

                    // Update tags
                    TagsDAO tagsDao = new TagsDAO(conn);
                    
                    // Remove existing tag associations
                    tagsDao.removeAllTagsFromPost(postId);

                    // Add new tags
                    if (tagsInput != null && !tagsInput.trim().isEmpty()) {
                        String[] tagNames = tagsInput.split(",");
                        for (String tagName : tagNames) {
                            tagName = tagName.trim().toLowerCase();
                            if (!tagName.isEmpty()) {
                                Tag tag = tagsDao.findByName(tagName);
                                if (tag == null) {
                                    tag = new Tag();
                                    tag.setName(tagName);
                                    tag = tagsDao.create(tag);
                                }
                                tagsDao.linkTagToPost(tag.getId(), postId);
                            }
                        }
                    }

                    conn.commit();
                    res.redirect("/posts");
                    return null;
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error updating post: " + e.getMessage();
            }
        });

        // Handle comment submission
        post("/posts/:id/comment", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            if (userId == null) {
                res.redirect("/login");
                return null;
            }

            int postId = Integer.parseInt(req.params(":id"));
            String content = req.queryParams("content");

            try (Connection conn = Database.getConnection()) {
                CommentDAO commentDao = new CommentDAO(conn);
                Comment comment = new Comment();
                comment.setPostId(postId);
                comment.setUserId(userId);
                comment.setContent(content);
                commentDao.create(comment);
                res.redirect("/posts");
                return null;
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error adding comment: " + e.getMessage();
            }
        });
    }
}
