package com.cardiolink.controllers;

import com.cardiolink.Models.User;

public interface UserAwareController {
    void setCurrentUser(User user);
}