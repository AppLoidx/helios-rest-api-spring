package com.apploidxxx.heliosrestapispring.entity.note;

/**
 * @author Arthur Kupriyanov
 */
public enum NoteType {
    PRIVATE, PUBLIC, UNKNOWN;

    public static NoteType getNoteType(String type){
        switch (type.toLowerCase()){
            case "private" : return NoteType.PRIVATE;
            case "public" : return NoteType.PUBLIC;
            default: return NoteType.UNKNOWN;
        }
    }
}
