package ryans.blog.app;

import ryans.blog.model.User;

public class AppGlobalState {

    private static final AppGlobalState instance = new AppGlobalState();
    private User currentUser;

    private AppGlobalState() {}

    public static AppGlobalState getInstance() {
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
