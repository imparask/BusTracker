package com.finalyear.login;

import android.app.Application;
import com.finalyear.login.model.User;

public class UserClient extends Application {
    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User conductor) {
        this.user = conductor;
    }

}
