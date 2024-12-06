package ryans.blog;

import static spark.Spark.*;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.time.LocalDateTime;
import ryans.blog.app.Database;
import ryans.blog.dao.*;
import ryans.blog.model.*;

public class BlogApplication {

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime.class,
            new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(
                    LocalDateTime src,
                    Type typeOfSrc,
                    JsonSerializationContext context
                ) {
                    return new JsonPrimitive(src.toString());
                }
            }
        )
        .registerTypeAdapter(
            LocalDateTime.class,
            new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(
                    JsonElement json,
                    Type type,
                    JsonDeserializationContext context
                ) throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString());
                }
            }
        )
        .create();

    public static void main(String[] args) {
        try {
            // Initialize database
            System.out.println("Initializing database...");
            //Database.initializeDatabase();
            Connection conn = Database.getConnection();
            UserDAO userDao = new UserDAO(conn);
            PostDAO postDao = new PostDAO(conn);
            System.out.println("Database initialized successfully");

            // Configure Spark
            port(8090);

            // Enable CORS for React frontend
            before((request, response) -> {
                response.header(
                    "Access-Control-Allow-Origin",
                    "http://localhost:3000"
                );
                response.header(
                    "Access-Control-Allow-Methods",
                    "GET,POST,PUT,DELETE,OPTIONS"
                );
                response.header("Access-Control-Allow-Headers", "*");
                response.header("Access-Control-Allow-Credentials", "true");
                response.type("application/json");
            });

            options("/*", (request, response) -> {
                String accessControlRequestHeaders = request.headers(
                    "Access-Control-Request-Headers"
                );
                if (accessControlRequestHeaders != null) {
                    response.header(
                        "Access-Control-Allow-Headers",
                        accessControlRequestHeaders
                    );
                }
                return "OK";
            });

            // User Routes
            post("/api/users", (req, res) -> {
                System.out.println(
                    "Received registration request with body: " + req.body()
                );
                try {
                    User user = gson.fromJson(req.body(), User.class);
                    System.out.println(
                        "Parsed user object: " + gson.toJson(user)
                    );

                    // Validate required fields
                    if (
                        user.getUsername() == null ||
                        user.getUsername().trim().isEmpty()
                    ) {
                        res.status(400);
                        return "Username is required";
                    }
                    if (
                        user.getPassword() == null ||
                        user.getPassword().trim().isEmpty()
                    ) {
                        res.status(400);
                        return "Password is required";
                    }

                    System.out.println("Creating user in database...");
                    User createdUser = userDao.create(user);
                    System.out.println(
                        "User created successfully: " + gson.toJson(createdUser)
                    );
                    return gson.toJson(createdUser);
                } catch (Exception e) {
                    System.err.println(
                        "Error creating user: " + e.getMessage()
                    );
                    e.printStackTrace();
                    res.status(500);
                    return "Error creating user: " + e.getMessage();
                }
            });

            post("/api/login", (req, res) -> {
                try {
                    User credentials = gson.fromJson(req.body(), User.class);

                    // Validate input
                    if (
                        credentials.getUsername() == null ||
                        credentials.getPassword() == null
                    ) {
                        res.status(400);
                        return "Username and password are required";
                    }

                    // Find user by username
                    User user = userDao.findByUsernameAndPassword(
                        credentials.getUsername(),
                        credentials.getPassword()
                    );

                    // Verify password (in a real app, use proper password hashing)
                    if (
                        user != null &&
                        user.getPassword().equals(credentials.getPassword())
                    ) {
                        return gson.toJson(user);
                    }

                    res.status(401);
                    return "Invalid username or password";
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return "Error during login: " + e.getMessage();
                }
            });

            get("/api/users", (req, res) -> gson.toJson(userDao.findAll()));

            get("/api/users/:id", (req, res) -> {
                int id = Integer.parseInt(req.params(":id"));
                User user = userDao.findById(id);
                if (user != null) {
                    return gson.toJson(user);
                }
                res.status(404);
                return "User not found";
            });

            // Post Routes
            post("/api/posts", (req, res) -> {
                try {
                    System.out.println(
                        "Received post request with body: " + req.body()
                    );
                    Post post = gson.fromJson(req.body(), Post.class);

                    // Validate post data
                    if (
                        post.getTitle() == null ||
                        post.getTitle().trim().isEmpty()
                    ) {
                        res.status(400);
                        return "Title is required";
                    }
                    if (
                        post.getContent() == null ||
                        post.getContent().trim().isEmpty()
                    ) {
                        res.status(400);
                        return "Content is required";
                    }
                    if (post.getUserId() == null) {
                        res.status(400);
                        return "Author ID is required";
                    }
                    // Create post
                    Post createdPost = postDao.create(post);
                    System.out.println(
                        "Created post: " + gson.toJson(createdPost)
                    );
                    return gson.toJson(createdPost);
                } catch (JsonSyntaxException e) {
                    System.err.println(
                        "Invalid JSON format: " + e.getMessage()
                    );
                    res.status(400);
                    return "Invalid request format";
                } catch (Exception e) {
                    System.err.println(
                        "Error creating post: " + e.getMessage()
                    );
                    e.printStackTrace();
                    res.status(500);
                    return "Error creating post: " + e.getMessage();
                }
            });

            get("/api/posts", (req, res) -> gson.toJson(postDao.findAll()));

            get("/api/posts/:id", (req, res) -> {
                Integer id = Integer.parseInt(req.params(":id"));
                Post post = postDao.findById(id);
                if (post != null) {
                    return gson.toJson(post);
                }
                res.status(404);
                return "Post not found";
            });

            get("/api/users/:id/posts", (req, res) -> {
                Integer authorId = Integer.parseInt(req.params(":id"));
                return gson.toJson(postDao.findByAuthor(authorId));
            });

            put("/api/posts/:id", (req, res) -> {
                Integer id = Integer.parseInt(req.params(":id"));
                Post post = gson.fromJson(req.body(), Post.class);
                if (postDao.update(post)) {
                    return gson.toJson(post);
                }
                res.status(404);
                return "Post not found";
            });

            delete("/api/posts/:id", (req, res) -> {
                try {
                    int postId = Integer.parseInt(req.params(":id"));
                    postDao.delete(postId);
                    res.status(204); // No content
                    return "";
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return "Error deleting post: " + e.getMessage();
                }
            });

            delete("/api/posts/:id", (req, res) -> {
                Integer id = Integer.parseInt(req.params(":id"));
                Integer userId = Integer.parseInt(req.queryParams("userId"));
                if (postDao.delete(id, userId)) {
                    return "Post deleted";
                }
                res.status(404);
                return "Post not found";
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
