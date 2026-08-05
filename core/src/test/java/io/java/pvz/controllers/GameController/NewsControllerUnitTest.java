package io.java.pvz.controllers.GameController;

import io.java.pvz.models.App;
import io.java.pvz.models.news.Message;
import io.java.pvz.models.news.MessageType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NewsControllerUnitTest {

    private NewsController controller;

    @Before
    public void setUp() throws Exception {
        App.getNews().getMessages().clear();
        controller = new NewsController();
    }

    private void addMessages(Message... messages) {
        for (Message m : messages) {
            App.getNews().addMessages(m);
        }
    }

    // ==========================================
    // Plant News Tests
    // ==========================================

    @Test
    public void testShowUnreadPlantNewsReturnsOnlyPlantMessages() {
        Message plantMsg = new Message("plant item", MessageType.PLANT);
        Message zombieMsg = new Message("zombie item", MessageType.ZOMBIE);
        addMessages(plantMsg, zombieMsg);

        String result = controller.showUnreadPlantNews();

        assertNotNull(result);
        assertTrue(result.contains("plant item"));
        assertFalse(result.contains("zombie item"));
        assertFalse(plantMsg.isUnread());
    }

    @Test
    public void testShowUnreadPlantNewsWhenNoPlantNewsReturnsNull() {
        Message zombieMsg = new Message("zombie item", MessageType.ZOMBIE);
        addMessages(zombieMsg);

        String result = controller.showUnreadPlantNews();

        assertNull(result);
    }

    // ==========================================
    // Zombie News Tests
    // ==========================================

    @Test
    public void testShowUnreadZombieNewsReturnsOnlyZombieMessages() {
        Message zombieMsg = new Message("zombie item", MessageType.ZOMBIE);
        addMessages(zombieMsg);

        String result = controller.showUnreadZombieNews();

        assertNotNull(result);
        assertTrue(result.contains("zombie item"));
        assertFalse(zombieMsg.isUnread());
    }

    @Test
    public void testShowUnreadZombieNewsWhenNoZombieNewsReturnsNull() {
        String result = controller.showUnreadZombieNews();
        assertNull(result);
    }

    // ==========================================
    // Level & Season News Tests
    // ==========================================

    @Test
    public void testShowUnreadLevelNewsReturnsOnlyLevelMessages() {
        Message levelMsg = new Message("level item", MessageType.LEVEL);
        addMessages(levelMsg);

        String result = controller.showUnreadLevelNews();

        assertNotNull(result);
        assertTrue(result.contains("level item"));
        assertFalse(levelMsg.isUnread());
    }

    @Test
    public void testShowUnreadSeasonNewsReturnsOnlySeasonMessages() {
        Message seasonMsg = new Message("season item", MessageType.SEASON);
        addMessages(seasonMsg);

        String result = controller.showUnreadSeasonNews();

        assertNotNull(result);
        assertTrue(result.contains("season item"));
        assertFalse(seasonMsg.isUnread());
    }

    // ==========================================
    // Minigame News Tests
    // ==========================================

    @Test
    public void testShowUnreadMinigameNewsReturnsOnlyMinigameMessages() {
        Message minigameMsg = new Message("minigame item", MessageType.MINIGAME);
        addMessages(minigameMsg);

        String result = controller.showUnreadMinigameNews();

        assertNotNull(result);
        assertTrue(result.contains("minigame item"));
        assertFalse(minigameMsg.isUnread());
    }

    // ==========================================
    // General Behavior Tests (Re-read & Formatting)
    // ==========================================

    @Test
    public void testShowUnreadNewsCalledTwiceSecondCallReturnsNull() {
        addMessages(new Message("plant item", MessageType.PLANT));

        controller.showUnreadPlantNews();
        String secondCall = controller.showUnreadPlantNews();

        assertNull(secondCall);
    }

    @Test
    public void testShowUnreadNewsMultipleMessagesNoTrailingNewline() {
        addMessages(
            new Message("plant1", MessageType.PLANT),
            new Message("plant2", MessageType.PLANT)
        );

        String result = controller.showUnreadPlantNews();

        assertNotNull(result);
        assertFalse(result.endsWith("\n"));
    }

    @Test
    public void testShowUnreadNewsExcludesAlreadyReadMessages() {
        Message readMsg = new Message("already read plant", MessageType.PLANT);
        readMsg.setUnread(false);
        Message unreadMsg = new Message("unread plant", MessageType.PLANT);
        addMessages(readMsg, unreadMsg);

        String result = controller.showUnreadPlantNews();

        assertNotNull(result);
        assertFalse(result.contains("already read plant"));
        assertTrue(result.contains("unread plant"));
    }
}
