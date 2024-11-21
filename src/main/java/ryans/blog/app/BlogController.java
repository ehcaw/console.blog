package main.java.ryans.blog.app;

import static spark.Spark.*;

import java.sql.*;
import java.util.*;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.*;

public class BlogController {

    public static void main(String[] args) {
        // Initialize Spark
        SparkConf conf = new SparkConf()
            .setAppName("BlogApp")
            .setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Initialize SQL context
        SparkSession spark = SparkSession.builder()
            .appName("BlogApp")
            .getOrCreate();
        Dataset<Row> posts;

        // Setup routes
        get("/", (req, res) -> {
            return "Welcome to the Blog! Use /posts to see blog posts.";
        });

        // Route to get all posts (accessible via curl or browser)
        get("/posts", (req, res) -> {
            res.type("text/html");
            posts = spark
                .read()
                .format("jdbc")
                .option("url", "jdbc:mysql://localhost:3306/blog_db")
                .option("dbtable", "Posts")
                .option("user", "root")
                .option("password", "password")
                .load();
            StringBuilder sb = new StringBuilder("<h1>Posts</h1>");
            for (Row row : posts.collectAsList()) {
                sb
                    .append("<h2>")
                    .append(row.getString(1))
                    .append("</h2>")
                    .append("<p>")
                    .append(row.getString(2))
                    .append("</p>");
            }
            return sb.toString();
        });

        // Route to add new post (via POST request)
        post("/posts", (req, res) -> {
            String title = req.queryParams("title");
            String content = req.queryParams("content");

            try (Connection conn = DatabaseConnector.getConnection()) {
                String query =
                    "INSERT INTO Posts (title, content, user_id) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, title);
                    stmt.setString(2, content);
                    stmt.setInt(3, 1); // Assuming a static user ID for simplicity
                    stmt.executeUpdate();
                }
            }
            return "Post added successfully!";
        });

        // Route to view individual post (optional)
        get("/posts/:id", (req, res) -> {
            int postId = Integer.parseInt(req.params(":id"));
            try (Connection conn = DatabaseConnector.getConnection()) {
                String query = "SELECT * FROM Posts WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, postId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return (
                                "<h2>" +
                                rs.getString("title") +
                                "</h2><p>" +
                                rs.getString("content") +
                                "</p>"
                            );
                        }
                    }
                }
            }
            return "Post not found!";
        });
    }
}
