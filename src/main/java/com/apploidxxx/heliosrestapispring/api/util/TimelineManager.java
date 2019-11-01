package com.apploidxxx.heliosrestapispring.api.util;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.timeline.Timeline;
import com.apploidxxx.heliosrestapispring.entity.user.timeline.TimelineTag;

/**
 * @author Arthur Kupriyanov
 */
public class TimelineManager {
    public static void addTimelineTo(User user, Timeline timeline){
        user.getTimelines().add(timeline);
    }

    public static void addQueueCreatedTimeline(User user, Queue queue){
        String queuePrivate = queue.getPassword()==null?"":"приватную";
        String text = String.format("Создал %s очередь %s [/%s]", queuePrivate, queue.getFullname(), queue.getName());
        Timeline timeline = new Timeline(user, text, TimelineTag.QUEUE);
        user.getTimelines().add(timeline);
    }

    public static void addQueueJoinedTimeline(User user, Queue queue){
        String text = String.format("Зашел в очередь %s", queue.getFullname());
        Timeline timeline = new Timeline(user, text, TimelineTag.QUEUE);
        user.getTimelines().add(timeline);
    }
}
