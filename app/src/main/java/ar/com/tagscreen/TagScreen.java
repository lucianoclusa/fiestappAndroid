package ar.com.tagscreen;

import android.app.Application;

import ar.com.tagscreen.entities.User;

/**
 * Created by luciano.clusa on 20/12/2016.
 */
public class TagScreen extends Application {
    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

}
