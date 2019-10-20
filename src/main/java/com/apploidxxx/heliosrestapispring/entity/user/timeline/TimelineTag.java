package com.apploidxxx.heliosrestapispring.entity.user.timeline;

/**
 * @author Arthur Kupriyanov
 */
public enum TimelineTag {
    COMMENTARY{
        @Override
        public String getColor() {
            return "#e17b77";
        }

        @Override
        public String toString() {
            return "Комментарий";
        }
    },
    SWAP {
        @Override
        public String getColor() {
            return "#FFDB14";
        }

        @Override
        public String toString() {
            return "Обмен мест";
        }
    },
    QUEUE {
        @Override
        public String getColor() {
            return "#018f69";
        }

        @Override
        public String toString() {
            return "Очередь";
        }
    };

    public abstract String getColor();
}
