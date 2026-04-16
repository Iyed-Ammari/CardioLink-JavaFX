package com.cardiolink.Controllers;

import com.cardiolink.Models.User;

public interface UserAwareController {
    void setCurrentUser(User user);
}