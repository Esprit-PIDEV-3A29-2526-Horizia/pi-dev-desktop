package tn.esprit.utils;

import tn.esprit.entities.User;

public interface UserAware {
    void setCurrentUser(User user);
}