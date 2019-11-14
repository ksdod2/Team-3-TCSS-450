package edu.uw.tcss450.team3chatapp.model;

import java.io.Serializable;

public class ChatMessageNotification implements Serializable {
    private final ChatMessage mMessage;
    private final int mRoom;


    public static class Builder {
        private final ChatMessage message;
        private int room;

        public Builder(ChatMessage message, int room) {
            this.message = message;
            this.room = room;
        }

        public ChatMessageNotification build() {
            return new ChatMessageNotification(this);
        }
    }

    private ChatMessageNotification(final Builder builder) {
        mMessage = builder.message;
        mRoom = builder.room;
    }

    public ChatMessage getMessage() {
        return mMessage;
    }

    public int getRoom() {
        return mRoom;
    }
}
