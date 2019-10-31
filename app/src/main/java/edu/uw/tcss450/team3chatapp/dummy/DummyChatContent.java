package edu.uw.tcss450.team3chatapp.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.tcss450.team3chatapp.model.Chat;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyChatContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Chat> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Chat> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createChat(i));
        }
    }

    private static void addItem(Chat item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getName(), item);
    }

    private static Chat createChat(int position) {
        return new Chat(position, "Chat " + position);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

}
