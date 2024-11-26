package ryans.blog.app.webapp; // Changed package declaration

import static spark.Spark.*;

import java.sql.*;
import ryans.blog.app.Database;
import spark.Spark;

public class BlogController {

    public static void main(String[] args) {
        // Set the port for the web server
        port(4567);

        // Setup routes
        get("/", (req, res) -> {
            return "Welcome to the Blog! Use /posts to see blog posts.";
        });

        // Route to get all posts (accessible via curl or browser)
        get("/posts", (req, res) -> {
            res.type("text/html");
            StringBuilder sb = new StringBuilder("<h1>Posts</h1>");

            try (Connection conn = Database.getConnection()) {
                String query = "SELECT * FROM Posts";
                try (
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery()
                ) {
                    while (rs.next()) {
                        sb
                            .append("<h2>")
                            .append(rs.getString("title"))
                            .append("</h2>")
                            .append("<p>")
                            .append(rs.getString("content"))
                            .append("</p>");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error fetching posts: " + e.getMessage();
            }

            return sb.toString();
        });

        // Route to add new post (via POST request)
        post("/posts", (req, res) -> {
            String title = req.queryParams("title");
            String content = req.queryParams("content");

            try (Connection conn = Database.getConnection()) {
                String query =
                    "INSERT INTO Posts (title, content, user_id) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, title);
                    stmt.setString(2, content);
                    stmt.setInt(3, 1); // Assuming a static user ID for simplicity
                    stmt.executeUpdate();
                }
                return "Post added successfully!";
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error adding post: " + e.getMessage();
            }
        });

        // Route to view individual post
        get("/posts/:id", (req, res) -> {
            res.type("text/html");
            int postId = Integer.parseInt(req.params(":id"));

            try (Connection conn = Database.getConnection()) {
                String query = "SELECT * FROM Posts WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, postId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return (
                                "<h2>" +
                                rs.getString("title") +
                                "</h2>" +
                                "<p>" +
                                rs.getString("content") +
                                "</p>"
                            );
                        }
                    }
                }
                return "Post not found!";
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error fetching post: " + e.getMessage();
            }
        });
    }
}
