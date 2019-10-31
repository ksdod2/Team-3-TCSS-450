package edu.uw.tcss450.team3chatapp.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.tcss450.team3chatapp.model.ChatMessage;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyMessageContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<ChatMessage> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, ChatMessage> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(ChatMessage item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getSender(), item);
    }

    private static ChatMessage createDummyItem(int position) {
        return new ChatMessage("Chatter " + position, "10-30-19 12:55", "Here's a message sent by chatter " + position);
    }

}
