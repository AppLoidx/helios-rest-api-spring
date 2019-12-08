package com.apploidxxx.heliosrestapispring.entity.commentary;

/**
 * @author Arthur Kupriyanov
 */
public enum CommentaryType {

    PRIVATE{
        @Override
        String getString() {
            return "private";
        }
    },
    PUBLIC{
        @Override
        String getString() {
            return "public";
        }
    };

    abstract String getString();

    public static CommentaryType getType(String key){
        for (CommentaryType type : CommentaryType.values()){
            if (type.getString().equals(key)){
                return type;
            }
        }

        return null;
    }
}
