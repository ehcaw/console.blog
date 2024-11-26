package ryans.blog.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserModel {

    private Long id;
    private String username;
    private LocalDateTime createdAt;

    // Default constructor
    public UserModel() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public UserModel(String username) {
        this();
        this.username = username;
        this.createdAt = LocalDateTime.now();
    }

    // Full constructor
    public UserModel(
        Long id,
        String username,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        String role
    ) {
        this();
        this.id = id;
        this.username = username;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods

    // Override methods for proper object comparison and representation
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return (
            Objects.equals(id, userModel.id) &&
            Objects.equals(username, userModel.username)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return (
            "UserModel{" + "id=" + id + ", username='" + username + '\'' + '}'
        );
    }

    // Builder pattern for flexible object creation
    public static class Builder {

        private final UserModel user;

        public Builder() {
            user = new UserModel();
        }

        public Builder withId(Long id) {
            user.setId(id);
            return this;
        }

        public Builder withUsername(String username) {
            user.setUsername(username);
            return this;
        }

        public UserModel build() {
            return user;
        }
    }
}
