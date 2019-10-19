package com.apploidxxx.heliosrestapispring.api.util;

import com.apploidxxx.heliosrestapispring.entity.user.Badge;
import com.apploidxxx.heliosrestapispring.entity.user.User;

/**
 * @author Arthur Kupriyanov
 */
public enum BadgesFactory {
    DEVELOPER{
        @Override
        public Badge getInstance(User user) {
            return new Badge("Developer", "danger", user);
        }
    },
    ADMIN {
        @Override
        public Badge getInstance(User user) {
            return new Badge("Admin", "warning", user);
        }
    },
    TEACHER {
        @Override
        public Badge getInstance(User user) {
            return new Badge("Teacher", "info", user);
        }
    };


    public abstract Badge getInstance(User user);

    public static BadgesFactory getBadge(String name){
        for (BadgesFactory badge : BadgesFactory.values()){
            if (badge.name().equals(name.toUpperCase())){
                return badge;
            }
        }

        return null;
    }
}
