package edu.uw.tcss450.team3chatapp.model;

import java.io.Serializable;

/**
 * A notification for use with received chat messages.
 */
public class ChatMessageNotification implements Serializable {

    /** The received chat message. */
    private final ChatMessage mMessage;
    /** The ID of the Chatroom that this message is part of. */
    private final int mRoom;

    /** A Builder to use in creating ChatMessageNotifications. */
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

    /**
     * Constructs a ChatMessageNotification from a prepared internal class Builder.
     * @param builder the builder with information for use
     */
    private ChatMessageNotification(final Builder builder) {
        mMessage = builder.message;
        mRoom = builder.room;
    }

    /**
     * Gets the ChatMessage in the notification.
     * @return the ChatMessage
     */
    public ChatMessage getMessage() {
        return mMessage;
    }

    /**
     * Gets the Chatroom ID that the chat message is part of.
     * @return the Chatroom ID the chat is from
     */
    public int getRoom() {
        return mRoom;
    }
}
