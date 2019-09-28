package com.apploidxxx.heliosrestapispring.entity.queue;


/**
 * @author Arthur Kupriyanov
 */
public enum GenerationType {
    ONE_WEEK, TWO_WEEKS, NOT_STATED;

    public static GenerationType getType(String type){
        if (type.toLowerCase().equals("one_week")) return GenerationType.ONE_WEEK;
        else if (type.toLowerCase().equals("two_weeks")) return GenerationType.TWO_WEEKS;
        else return GenerationType.NOT_STATED;
    }
}
