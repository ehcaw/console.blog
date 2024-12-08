package ryans.blog.app.webapp;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import ryans.blog.app.Database;
import ryans.blog.dao.CommentDAO;
import ryans.blog.dao.UserDAO;
import ryans.blog.model.Comment;
import ryans.blog.model.User;
import spark.Request;
import spark.Response;

import static spark.Spark.*;
import spark.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import ryans.blog.dao.PostDAO;
import ryans.blog.dao.TagsDAO;
import ryans.blog.model.Post;
import ryans.blog.model.Tag;
import ryans.blog.app.cli.utils.Hash;
import spark.Session;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Date;

public class BlogController {

    private static final Gson gson = new GsonBuilder().create();

    private static Integer getCurrentUserId(Request req) {
        Session session = req.session(false);
        if (session != null) {
            return session.attribute("userId");
        }
        return null;
    }

    public static void main(String[] args) {
        // Set the port for the web server
        port(9091);

        // Enable CORS for React frontend
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "http://localhost:3000");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
            response.header("Access-Control-Allow-Credentials", "true");
            response.type("application/json");
        });

        // Login endpoint
        post("/api/login", (req, res) -> {
            System.out.println("Login Request Received:");
            System.out.println("Full Request Body: " + req.body());
            System.out.println("Query Params: " + req.queryParams());
            
            // Print out all query parameters
            if (req.queryParams() != null) {
                for (String param : req.queryParams()) {
                    System.out.println("Query Param - " + param + ": " + req.queryParams(param));
                }
            }

            String username = null;
            String password = null;

            // Try query params first
            username = req.queryParams("username");
            password = req.queryParams("password");

            // If query params are null, parse from form body
            if (username == null || password == null) {
                String body = req.body();
                System.out.println("Attempting to parse body: " + body);
                
                if (body != null && !body.isEmpty()) {
                    // Try different parsing methods
                    try {
                        // Method 1: Split by &
                        String[] params = body.split("&");
                        for (String param : params) {
                            String[] keyValue = param.split("=");
                            if (keyValue.length == 2) {
                                if ("username".equals(keyValue[0])) {
                                    username = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                                } else if ("password".equals(keyValue[0])) {
                                    password = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing body by &: " + e.getMessage());
                    }

                    // Method 2: If first method fails, try direct string contains
                    if (username == null) {
                        int usernameIndex = body.indexOf("username=");
                        if (usernameIndex != -1) {
                            int nextParamIndex = body.indexOf("&", usernameIndex);
                            username = nextParamIndex != -1 
                                ? body.substring(usernameIndex + 9, nextParamIndex)
                                : body.substring(usernameIndex + 9);
                            username = URLDecoder.decode(username, StandardCharsets.UTF_8.name());
                        }
                    }

                    if (password == null) {
                        int passwordIndex = body.indexOf("password=");
                        if (passwordIndex != -1) {
                            int nextParamIndex = body.indexOf("&", passwordIndex);
                            password = nextParamIndex != -1 
                                ? body.substring(passwordIndex + 9, nextParamIndex)
                                : body.substring(passwordIndex + 9);
                            password = URLDecoder.decode(password, StandardCharsets.UTF_8.name());
                        }
                    }
                }
            }

            System.out.println("Final Extracted Username: " + username);
            System.out.println("Final Extracted Password Length: " + (password != null ? password.length() : "null"));

            // Validate input
            if (username == null || username.trim().isEmpty()) {
                System.out.println("Username validation failed");
                res.status(400);
                return gson.toJson(Map.of("error", "Username is required"));
            }
            if (password == null || password.trim().isEmpty()) {
                System.out.println("Password validation failed");
                res.status(400);
                return gson.toJson(Map.of("error", "Password is required"));
            }

            try (Connection conn = Database.getConnection()) {
                UserDAO userDao = new UserDAO(conn);
                User user = userDao.findByUsername(username);

                System.out.println("User Found: " + (user != null));

                if (user != null) {
                    System.out.println("Hashing input password...");
                    String hashedInputPassword = Hash.hashPassword(password);
                    System.out.println("Hashed input password: " + hashedInputPassword);
                    System.out.println("Stored Password Hash: " + user.getPassword());
                    System.out.println("Input Password Hash: " + hashedInputPassword);

                    System.out.println("Comparing hashed input password with stored password hash...");
                    boolean passwordsMatch = user.getPassword().equals(hashedInputPassword);
                    System.out.println("Passwords match: " + passwordsMatch);

                    if (passwordsMatch) {
                        System.out.println("Login successful, setting session for user: " + user.getUserId());
                        req.session(true); // Create session if it doesn't exist
                        req.session().attribute("userId", user.getUserId());
                        req.session().attribute("username", user.getUsername());
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("user", Map.of(
                            "id", user.getUserId(),
                            "username", user.getUsername()
                        ));
                        return gson.toJson(response);
                    } else {
                        System.out.println("Password mismatch for user: " + username);
                    }
                } else {
                    System.out.println("No user found with username: " + username);
                }

                res.status(401);
                return gson.toJson(Map.of("error", "Invalid username or password"));
            } catch (Exception e) {
                // Log the full stack trace to console
                System.err.println("Login Error Details:");
                e.printStackTrace(System.err);

                // Prepare a detailed error response
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Internal server error",
                    "details", sw.toString()
                ));
            }
        });

        // Register endpoint
        post("/api/register", (req, res) -> {
            System.out.println("Register Request Received:");
            System.out.println("Query Params: " + req.queryParams());
            System.out.println("Request Body Raw: " + req.body());

            String username = req.queryParams("username");
            String password = req.queryParams("password");

            // If query params are null, parse from form body
            if (username == null || password == null) {
                String body = req.body();
                if (body != null && !body.isEmpty()) {
                    String[] params = body.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2) {
                            if ("username".equals(keyValue[0])) {
                                username = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                            } else if ("password".equals(keyValue[0])) {
                                password = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                            }
                        }
                    }
                }
            }

            System.out.println("Extracted Username: " + username);

            try (Connection conn = Database.getConnection()) {
                UserDAO userDao = new UserDAO(conn);
                
                if (userDao.findByUsername(username) != null) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Username already exists"));
                }

                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password); // Remove double hashing

                User createdUser = userDao.create(newUser);
                
                return gson.toJson(Map.of(
                    "success", true,
                    "user", Map.of(
                        "id", createdUser.getUserId(),
                        "username", createdUser.getUsername()
                    )
                ));
            } catch (Exception e) {
                // Log the full stack trace to console
                System.err.println("Register Error Details:");
                e.printStackTrace(System.err);

                // Prepare a detailed error response
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Internal server error",
                    "details", sw.toString()
                ));
            }
        });

        // Search posts endpoint (must be before the :id endpoint)
        get("/api/posts/search", (req, res) -> {
            res.type("application/json");
            System.out.println("Search request received with params:");
            System.out.println("title: " + req.queryParams("title"));
            System.out.println("author: " + req.queryParams("author"));
            System.out.println("tags: " + req.queryParams("tags"));

            try (Connection conn = Database.getConnection()) {
                PostDAO postDao = new PostDAO(conn);
                CommentDAO commentDao = new CommentDAO(conn);
                UserDAO userDao = new UserDAO(conn);
                TagsDAO tagsDao = new TagsDAO(conn);

                String title = req.queryParams("title");
                String author = req.queryParams("author");
                String tagsParam = req.queryParams("tags");
                List<String> tags = tagsParam != null ? Arrays.asList(tagsParam.split(",")) : null;

                System.out.println("Executing search with:");
                System.out.println("title: " + title);
                System.out.println("author: " + author);
                System.out.println("tags: " + tags);

                List<Post> posts = postDao.searchPosts(title, author, tags);
                System.out.println("Found " + posts.size() + " posts");

                List<Map<String, Object>> postsData = new ArrayList<>();

                // Enrich posts with comments, author, and tags
                for (Post post : posts) {
                    User postAuthor = userDao.findById(post.getUserId());
                    if (postAuthor == null) {
                        System.out.println("Warning: Author not found for post " + post.getId());
                        continue;
                    }

                    List<Comment> comments = commentDao.findByPostId(post.getId());
                    List<Tag> postTags = tagsDao.findByPostId(post.getId());
                    
                    List<Map<String, Object>> commentsData = new ArrayList<>();
                    for (Comment comment : comments) {
                        User commentAuthor = userDao.findById(comment.getUserId());
                        if (commentAuthor != null) {
                            commentsData.add(Map.of(
                                "id", comment.getId(),
                                "content", comment.getContent(),
                                "createdAt", comment.getCreatedAt(),
                                "author", Map.of(
                                    "id", commentAuthor.getUserId(),
                                    "username", commentAuthor.getUsername()
                                )
                            ));
                        }
                    }

                    List<String> tagNames = postTags.stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList());
                    post.setTags(tagNames);

                    Map<String, Object> postData = new HashMap<>();
                    postData.put("id", post.getId());
                    postData.put("title", post.getTitle());
                    postData.put("description", post.getDescription());
                    postData.put("content", post.getContent());
                    postData.put("createdAt", post.getCreatedAt());
                    postData.put("author", Map.of(
                        "id", postAuthor.getUserId(),
                        "username", postAuthor.getUsername()
                    ));
                    postData.put("tags", tagNames);
                    postData.put("comments", commentsData);
                    
                    postsData.add(postData);
                }

                String response = gson.toJson(Map.of(
                    "success", true,
                    "posts", postsData
                ));
                System.out.println("Sending response: " + response);
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                String errorResponse = gson.toJson(Map.of(
                    "success", false,
                    "error", "Failed to search posts: " + e.getMessage()
                ));
                System.out.println("Sending error response: " + errorResponse);
                return errorResponse;
            }
        });

        // Get all posts
        get("/api/posts", (req, res) -> {
            res.type("application/json");
            try (Connection conn = Database.getConnection()) {
                PostDAO postDao = new PostDAO(conn);
                UserDAO userDao = new UserDAO(conn);
                TagsDAO tagsDao = new TagsDAO(conn);
                CommentDAO commentDao = new CommentDAO(conn);

                List<Post> posts = postDao.findAll();
                List<Map<String, Object>> postsData = new ArrayList<>();

                for (Post post : posts) {
                    User author = userDao.findById(post.getUserId());
                    List<Tag> tags = tagsDao.findByPostId(post.getId());
                    List<Comment> comments = commentDao.findByPostId(post.getId());

                    List<Map<String, Object>> commentsData = new ArrayList<>();
                    for (Comment comment : comments) {
                        User commentAuthor = userDao.findById(comment.getUserId());
                        commentsData.add(Map.of(
                            "id", comment.getId(),
                            "content", comment.getContent(),
                            "createdAt", comment.getCreatedAt(),
                            "author", Map.of(
                                "id", commentAuthor.getUserId(),
                                "username", commentAuthor.getUsername()
                            )
                        ));
                    }
                    
                    postsData.add(Map.of(
                        "id", post.getId(),
                        "title", post.getTitle(),
                        "description", post.getDescription(),
                        "content", post.getContent(),
                        "createdAt", post.getCreatedAt(),
                        "author", Map.of(
                            "id", author.getUserId(),
                            "username", author.getUsername()
                        ),
                        "tags", tags.stream().map(Tag::getName).collect(Collectors.toList()),
                        "comments", commentsData
                    ));
                }
                
                return gson.toJson(Map.of("success", true, "posts", postsData));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Internal server error"));
            }
        });

        // Get a single post by ID
        get("/api/posts/:id", (req, res) -> {
            res.type("application/json");
            String postIdParam = req.params(":id");

            if (postIdParam == null || postIdParam.trim().isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("success", false, "error", "Post ID is required"));
            }

            try {
                int postId = Integer.parseInt(postIdParam.trim());

                try (Connection conn = Database.getConnection()) {
                    PostDAO postDao = new PostDAO(conn);
                    UserDAO userDao = new UserDAO(conn);
                    TagsDAO tagsDao = new TagsDAO(conn);
                    CommentDAO commentDao = new CommentDAO(conn);

                    Post post = postDao.findById(postId);
                    if (post == null) {
                        res.status(404);
                        return gson.toJson(Map.of("success", false, "error", "Post not found"));
                    }

                    User author = userDao.findById(post.getUserId());
                    List<Tag> tags = tagsDao.findByPostId(post.getId());
                    List<Comment> comments = commentDao.findByPostId(post.getId());

                    List<Map<String, Object>> commentsData = new ArrayList<>();
                    for (Comment comment : comments) {
                        User commentAuthor = userDao.findById(comment.getUserId());
                        commentsData.add(Map.of(
                            "id", comment.getId(),
                            "content", comment.getContent(),
                            "createdAt", comment.getCreatedAt(),
                            "author", Map.of(
                                "id", commentAuthor.getUserId(),
                                "username", commentAuthor.getUsername()
                            )
                        ));
                    }

                    Map<String, Object> postData = new HashMap<>();
                    postData.put("id", post.getId());
                    postData.put("title", post.getTitle());
                    postData.put("description", post.getDescription());
                    postData.put("content", post.getContent());
                    postData.put("createdAt", post.getCreatedAt());
                    postData.put("author", Map.of(
                        "id", author.getUserId(),
                        "username", author.getUsername()
                    ));
                    postData.put("tags", tags.stream().map(Tag::getName).collect(Collectors.toList()));
                    postData.put("comments", commentsData);

                    return gson.toJson(Map.of("success", true, "post", postData));
                }
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("success", false, "error", "Invalid post ID format"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of(
                    "success", false,
                    "error", "Internal server error"
                ));
            }
        });

        // Create post
        post("/api/posts", (req, res) -> {
            res.type("application/json");
            Integer userId = getCurrentUserId(req);
            if (userId == null) {
                res.status(401);
                return gson.toJson(Map.of(
                    "success", false,
                    "error", "Not logged in"
                ));
            }

            Connection conn = null;
            try {
                // Parse the request body
                String body = req.body();
                System.out.println("Received post data: " + body);
                
                Post newPost;
                try {
                    newPost = gson.fromJson(body, Post.class);
                } catch (JsonSyntaxException e) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "success", false,
                        "error", "Invalid post data format",
                        "details", e.getMessage()
                    ));
                }
                
                if (newPost == null) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "success", false,
                        "error", "Post data is required"
                    ));
                }
                
                // Validate required fields
                if (newPost.getTitle() == null || newPost.getTitle().trim().isEmpty()) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "success", false,
                        "error", "Title is required"
                    ));
                }

                if (newPost.getContent() == null || newPost.getContent().trim().isEmpty()) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "success", false,
                        "error", "Content is required"
                    ));
                }
                
                newPost.setUserId(userId);

                // Start transaction
                conn = Database.getConnection();
                conn.setAutoCommit(false);
                
                try {
                    PostDAO postDao = new PostDAO(conn);
                    TagsDAO tagsDao = new TagsDAO(conn);

                    // Create the post
                    System.out.println("Creating post with title: " + newPost.getTitle());
                    Post createdPost = postDao.create(newPost);
                    System.out.println("Created post with ID: " + createdPost.getId());

                    // Handle tags if present
                    List<String> tags = newPost.getTags();
                    if (tags != null && !tags.isEmpty()) {
                        System.out.println("Processing tags: " + tags);
                        for (String tagName : tags) {
                            if (tagName != null) {
                                tagName = tagName.trim().toLowerCase();
                                if (!tagName.isEmpty()) {
                                    Tag tag = tagsDao.findByName(tagName);
                                    if (tag == null) {
                                        tag = new Tag();
                                        tag.setName(tagName);
                                        tag = tagsDao.create(tag);
                                    }
                                    tagsDao.linkTagToPost(tag.getId(), createdPost.getId());
                                }
                            }
                        }
                    }

                    // Get the complete post data with tags
                    Post completePost = postDao.findById(createdPost.getId());
                    if (completePost != null) {
                        List<Tag> postTags = tagsDao.findByPostId(completePost.getId());
                        completePost.setTags(postTags.stream()
                            .map(Tag::getName)
                            .collect(Collectors.toList()));
                    }

                    conn.commit();
                    System.out.println("Successfully created post: " + completePost.getId());
                    return gson.toJson(Map.of(
                        "success", true,
                        "post", completePost
                    ));
                } catch (Exception e) {
                    if (conn != null) {
                        try {
                            System.err.println("Transaction is being rolled back");
                            conn.rollback();
                        } catch (SQLException ex) {
                            System.err.println("Error rolling back transaction");
                            ex.printStackTrace();
                        }
                    }
                    throw e;
                }
            } catch (Exception e) {
                System.err.println("Create Post Error Details:");
                e.printStackTrace(System.err);

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                res.status(500);
                return gson.toJson(Map.of(
                    "success", false,
                    "error", "Failed to create post",
                    "details", sw.toString()
                ));
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Update post
        put("/api/posts/:id", (req, res) -> {
            res.type("application/json");
            Integer userId = req.session().attribute("userId");
            
            if (userId == null) {
                res.status(401);
                return gson.toJson(Map.of("success", false, "error", "Not logged in"));
            }

            int postId = Integer.parseInt(req.params(":id"));

            try (Connection conn = Database.getConnection()) {
                PostDAO postDao = new PostDAO(conn);
                UserDAO userDao = new UserDAO(conn);
                TagsDAO tagsDao = new TagsDAO(conn);
                CommentDAO commentDao = new CommentDAO(conn);

                Post existingPost = postDao.findById(postId);

                if (existingPost == null) {
                    res.status(404);
                    return gson.toJson(Map.of("success", false, "error", "Post not found"));
                }

                if (existingPost.getUserId() != userId) {
                    res.status(403);
                    return gson.toJson(Map.of("success", false, "error", "Not authorized to update this post"));
                }

                try {
                    Post updatedPost = gson.fromJson(req.body(), Post.class);

                    if (updatedPost.getTitle() == null || updatedPost.getTitle().trim().isEmpty()) {
                        res.status(400);
                        return gson.toJson(Map.of("success", false, "error", "Title is required"));
                    }
                    if (updatedPost.getContent() == null || updatedPost.getContent().trim().isEmpty()) {
                        res.status(400);
                        return gson.toJson(Map.of("success", false, "error", "Content is required"));
                    }

                    existingPost.setTitle(updatedPost.getTitle().trim());
                    existingPost.setDescription(updatedPost.getDescription() != null ? updatedPost.getDescription().trim() : "");
                    existingPost.setContent(updatedPost.getContent().trim());
                    
                    conn.setAutoCommit(false);
                    try {
                        boolean updated = postDao.update(existingPost);
                        if (!updated) {
                            conn.rollback();
                            res.status(500);
                            return gson.toJson(Map.of("success", false, "error", "Failed to update post"));
                        }

                        if (updatedPost.getTags() != null) {
                            tagsDao.removeAllTagsFromPost(existingPost.getId());
                            
                            for (String tagName : updatedPost.getTags()) {
                                tagName = tagName.trim().toLowerCase();
                                if (!tagName.isEmpty()) {
                                    Tag tag = tagsDao.findByName(tagName);
                                    if (tag == null) {
                                        tag = new Tag();
                                        tag.setName(tagName);
                                        tag = tagsDao.create(tag);
                                    }
                                    tagsDao.linkTagToPost(tag.getId(), existingPost.getId());
                                }
                            }
                        }

                        conn.commit();

                        // Get the updated post with all related data
                        Post finalPost = postDao.findById(postId);
                        User author = userDao.findById(finalPost.getUserId());
                        List<Tag> tags = tagsDao.findByPostId(finalPost.getId());
                        List<Comment> comments = commentDao.findByPostId(finalPost.getId());
                        
                        List<Map<String, Object>> commentsData = new ArrayList<>();
                        for (Comment comment : comments) {
                            User commentAuthor = userDao.findById(comment.getUserId());
                            commentsData.add(Map.of(
                                "id", comment.getId(),
                                "content", comment.getContent(),
                                "createdAt", comment.getCreatedAt(),
                                "author", Map.of(
                                    "id", commentAuthor.getUserId(),
                                    "username", commentAuthor.getUsername()
                                )
                            ));
                        }

                        Map<String, Object> postData = new HashMap<>();
                        postData.put("id", finalPost.getId());
                        postData.put("title", finalPost.getTitle());
                        postData.put("description", finalPost.getDescription());
                        postData.put("content", finalPost.getContent());
                        postData.put("createdAt", finalPost.getCreatedAt());
                        postData.put("author", Map.of(
                            "id", author.getUserId(),
                            "username", author.getUsername()
                        ));
                        postData.put("tags", tags.stream().map(Tag::getName).collect(Collectors.toList()));
                        postData.put("comments", commentsData);

                        return gson.toJson(Map.of("success", true, "post", postData));
                    } catch (Exception e) {
                        conn.rollback();
                        throw e;
                    }
                } catch (JsonSyntaxException e) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "success", false,
                        "error", "Invalid request format"
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of(
                    "success", false,
                    "error", "Internal server error"
                ));
            }
        });

        // Delete post
        delete("/api/posts/:id", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            System.out.println("Delete Post - User ID from session: " + userId);
            
            if (userId == null) {
                res.status(401);
                return gson.toJson(Map.of("error", "Not logged in"));
            }

            int postId = Integer.parseInt(req.params(":id"));
            System.out.println("Delete Post - Post ID: " + postId);

            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false); // Start transaction

                PostDAO postDao = new PostDAO(conn);
                Post post = postDao.findById(postId);
                
                System.out.println("Delete Post - Found post: " + (post != null));
                if (post != null) {
                    System.out.println("Delete Post - Post user ID: " + post.getUserId());
                }

                if (post == null) {
                    conn.rollback();
                    res.status(404);
                    return gson.toJson(Map.of("error", "Post not found"));
                }

                if (post.getUserId() != userId) {
                    conn.rollback();
                    res.status(403);
                    return gson.toJson(Map.of("error", "Not authorized to delete this post"));
                }

                try {
                    // Delete associated comments first
                    CommentDAO commentDao = new CommentDAO(conn);
                    System.out.println("Delete Post - Removing comments");
                    commentDao.deleteByPostId(postId);

                    // Delete associated tags
                    TagsDAO tagsDao = new TagsDAO(conn);
                    System.out.println("Delete Post - Removing tags");
                    tagsDao.removeAllTagsFromPost(postId);

                    // Delete the post with user ID check
                    System.out.println("Delete Post - Deleting post");
                    boolean deleted = postDao.delete(postId, userId);
                    System.out.println("Delete Post - Delete result: " + deleted);

                    if (!deleted) {
                        conn.rollback();
                        res.status(404);
                        return gson.toJson(Map.of("error", "Failed to delete post"));
                    }

                    conn.commit(); // Commit transaction
                    return gson.toJson(Map.of("success", true));
                } catch (Exception e) {
                    conn.rollback(); // Rollback on error
                    System.err.println("Delete Post - Error during deletion:");
                    e.printStackTrace(System.err);
                    throw e;
                }
            } catch (Exception e) {
                System.err.println("Delete Post - Error:");
                e.printStackTrace(System.err);
                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Internal server error",
                    "message", e.getMessage(),
                    "type", e.getClass().getName()
                ));
            }
        });

        // Add a comment to a post
        post("/api/posts/:postId/comments", (req, res) -> {
            res.type("application/json");
            
            // Get user ID from session
            Integer userId = getCurrentUserId(req);
            if (userId == null) {
                System.out.println("User not logged in");
                res.status(401);
                return gson.toJson(Map.of("error", "You must be logged in to comment"));
            }
            System.out.println("User ID: " + userId);

            try {
                int postId = Integer.parseInt(req.params("postId"));

                // Parse request body
                Type mapType = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> requestMap = gson.fromJson(req.body(), mapType);
                
                if (requestMap == null || !requestMap.containsKey("content")) {
                    System.out.println("Request body is null or missing content");
                    res.status(400);
                    return gson.toJson(Map.of("error", "Comment content is required"));
                }

                String content = requestMap.get("content").trim();
                if (content.isEmpty()) {
                    System.out.println("Content is empty after trim");
                    res.status(400);
                    return gson.toJson(Map.of("error", "Comment content cannot be empty"));
                }

                try (Connection conn = Database.getConnection()) {
                    // Create the comment
                    Comment comment = new Comment();
                    comment.setContent(content);
                    comment.setPostId(postId);
                    comment.setUserId(userId);
                    
                    CommentDAO commentDao = new CommentDAO(conn);
                    comment = commentDao.create(comment);
                    
                    if (comment == null) {
                        System.out.println("Failed to create comment");
                        res.status(500);
                        return gson.toJson(Map.of("error", "Failed to create comment"));
                    }

                    // Get author info
                    UserDAO userDao = new UserDAO(conn);
                    User author = userDao.findById(userId);

                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("id", comment.getId());
                    commentData.put("content", comment.getContent());
                    commentData.put("postId", comment.getPostId());
                    commentData.put("userId", comment.getUserId());
                    commentData.put("createdAt", comment.getCreatedAt());
                    commentData.put("author", Map.of(
                        "id", author.getUserId(),
                        "username", author.getUsername()
                    ));

                    return gson.toJson(Map.of(
                        "success", true,
                        "comment", commentData
                    ));
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid post ID format");
                res.status(400);
                return gson.toJson(Map.of("error", "Invalid post ID"));
            } catch (Exception e) {
                System.out.println("Error processing request: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Internal server error"));
            }
        });

        // Update a comment
        put("/api/comments/:commentId", (req, res) -> {
            res.type("application/json");
            
            // Check if user is authenticated
            Integer userId = getCurrentUserId(req);
            if (userId == null) {
                res.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }

            // Parse comment ID
            int commentId;
            try {
                commentId = Integer.parseInt(req.params("commentId"));
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "Invalid comment ID"));
            }

            // Parse updated comment
            Comment updatedComment;
            try {
                updatedComment = gson.fromJson(req.body(), Comment.class);
                if (updatedComment.getContent() == null || updatedComment.getContent().trim().isEmpty()) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Comment content cannot be empty"));
                }
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "Invalid comment data"));
            }

            try (Connection conn = Database.getConnection()) {
                CommentDAO commentDao = new CommentDAO(conn);
                
                // Check if comment exists and belongs to user
                Comment existingComment = commentDao.findById(commentId);
                if (existingComment == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Comment not found"));
                }
                if (existingComment.getUserId() != userId) {
                    res.status(403);
                    return gson.toJson(Map.of("error", "Not authorized to edit this comment"));
                }

                // Update comment
                updatedComment.setId(commentId);
                updatedComment.setUserId(userId);
                updatedComment.setPostId(existingComment.getPostId());
                updatedComment.setCreatedAt(existingComment.getCreatedAt());
                
                boolean updated = commentDao.update(updatedComment);
                if (!updated) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Failed to update comment"));
                }

                return gson.toJson(Map.of("success", true));
            } catch (SQLException e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to update comment"));
            }
        });

        // Delete a comment
        delete("/api/comments/:commentId", (req, res) -> {
            res.type("application/json");
            
            // Check if user is authenticated
            Integer userId = getCurrentUserId(req);
            if (userId == null) {
                res.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }

            // Parse comment ID
            int commentId;
            try {
                commentId = Integer.parseInt(req.params("commentId"));
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "Invalid comment ID"));
            }

            try (Connection conn = Database.getConnection()) {
                CommentDAO commentDao = new CommentDAO(conn);
                
                // Check if comment exists and belongs to user
                Comment existingComment = commentDao.findById(commentId);
                if (existingComment == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Comment not found"));
                }
                if (existingComment.getUserId() != userId) {
                    res.status(403);
                    return gson.toJson(Map.of("error", "Not authorized to delete this comment"));
                }

                // Delete comment
                boolean deleted = commentDao.delete(commentId, userId);
                if (!deleted) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Failed to delete comment"));
                }

                return gson.toJson(Map.of("success", true));
            } catch (SQLException e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to delete comment"));
            }
        });
    }
}
